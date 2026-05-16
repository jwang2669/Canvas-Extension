package com.oiloncanvas.backend.service;

import com.oiloncanvas.backend.dto.canvasapi.CanvasCourse;
import com.oiloncanvas.backend.dto.canvasapi.CanvasAssignment;
import com.oiloncanvas.backend.entity.AssignmentAdditions;
import com.oiloncanvas.backend.entity.AssignmentCache;
import com.oiloncanvas.backend.entity.CourseCache;
import com.oiloncanvas.backend.entity.OocUser;
import com.oiloncanvas.backend.repository.AssignmentAdditionsRepository;
import com.oiloncanvas.backend.repository.AssignmentCacheRepository;
import com.oiloncanvas.backend.repository.CourseCacheRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.Map;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Replaces {@link CourseCache} rows for a user when Canvas enrollment data is refreshed. Deletes
 * dependent assignment rows first to satisfy FK constraints.
 */
@Service
public class CourseCacheSyncService {

  private final CourseCacheRepository courseCacheRepository;
  private final AssignmentCacheRepository assignmentCacheRepository;
  private final AssignmentAdditionsRepository assignmentAdditionsRepository;

  public CourseCacheSyncService(
      CourseCacheRepository courseCacheRepository,
      AssignmentCacheRepository assignmentCacheRepository,
      AssignmentAdditionsRepository assignmentAdditionsRepository) {
    this.courseCacheRepository = courseCacheRepository;
    this.assignmentCacheRepository = assignmentCacheRepository;
    this.assignmentAdditionsRepository = assignmentAdditionsRepository;
  }

  /**
   * Deletes all cached courses (and assignments) for the user, then inserts the given Canvas
   * courses.
   *
   * @param user persisted OOC user (must have {@code oocId})
   * @param courses filtered active courses from Canvas (same list as API returns)
   */
  @Transactional
  public void replaceForUser(OocUser user, List<CanvasCourse> courses, Map<Long, List<CanvasAssignment>> assignments) {
    if (user == null || user.getOocId() == null) {
      return;
    }
    for (CourseCache existing : courseCacheRepository.findByOocUser(user)) {
      for (AssignmentCache ac : assignmentCacheRepository.findByCourseCache(existing)) {
        AssignmentAdditions addition = assignmentAdditionsRepository.findByAssignmentCache(ac);
        if (addition != null) {
          assignmentAdditionsRepository.delete(addition);
        }
        assignmentCacheRepository.delete(ac);
      }
      courseCacheRepository.delete(existing);
    }

    for (CanvasCourse c : courses) {
      CourseCache row = new CourseCache();
      row.setOocUser(user);
      row.setCanvasCourseId((int) c.getId());
      row.setCourseName(
          c.getName() != null && !c.getName().isBlank() ? c.getName().trim() : "Course " + c.getId());
      String code = c.getCourseCode();
      row.setCourseCode(
          code != null && !code.isBlank() ? code.trim() : "ID-" + c.getId());
      CourseCache savedRow = courseCacheRepository.save(row);

      List<CanvasAssignment> courseAssignments = 
        assignments.getOrDefault(c.getId(), Collections.emptyList());
      for (CanvasAssignment a : courseAssignments) {
        AssignmentCache ac = new AssignmentCache();
        ac.setCourseCache(savedRow);
        ac.setCanvasAssignmentId((int) a.getId());
        ac.setAssignmentName(a.getName());
        ac.setDueDate(parseDueDateTime(a.getDueAt()));
        assignmentCacheRepository.save(ac);
      }
    }
  }

  /**
   * Parses due date strings that may be full timestamps or local dates.
    *
    * @param dueDate date/time string from request
    * @return parsed LocalDateTime or null when input is null
    * @throws IllegalArgumentException when value is non-null but not parseable
   */
  private static LocalDateTime parseDueDateTime(String dueDate) {
    if (dueDate == null) {
      return null;
    }

    try {
      return OffsetDateTime.parse(dueDate).toLocalDateTime();
    } catch (DateTimeParseException ignored) {
    }

    try {
      return LocalDateTime.parse(dueDate);
    } catch (DateTimeParseException ignored) {
    }

    try {
      return LocalDate.parse(dueDate).atStartOfDay();
    } catch (DateTimeParseException ignored) {
    }

    throw new IllegalArgumentException(
        "Task dueDate must be an ISO date or date-time string.");
  }
}
