package com.oiloncanvas.backend.service;

import com.oiloncanvas.backend.dto.SessionRequest;
import com.oiloncanvas.backend.dto.SessionResponse;
import com.oiloncanvas.backend.dto.canvasapi.CanvasCourse;
import com.oiloncanvas.backend.dto.canvasapi.CanvasAssignment;
import com.oiloncanvas.backend.dto.canvasapi.CanvasUser;
import com.oiloncanvas.backend.entity.OocUser;
import com.oiloncanvas.backend.repository.OocUserRepository;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/**
 * Handles starting and managing user sessions.
 * Also converts Canvas API user/course data into our own session format.
 */
@Service
public class SessionService {

  private final OocUserRepository oocUserRepository;
  private final CourseCacheSyncService courseCacheSyncService;

  public SessionService(
      OocUserRepository oocUserRepository, CourseCacheSyncService courseCacheSyncService) {
    this.oocUserRepository = oocUserRepository;
    this.courseCacheSyncService = courseCacheSyncService;
  }

  /**
   * Start a session from a manual request
   *
   * validates and normalizes
   * request data, then returns a stateless SessionResponse with generated
   * session metadata
   *
   * @param request contains userId and courseIds
   * @return a new SessionResponse
   */
  public SessionResponse startSession(SessionRequest request) {
    if (request == null) {
      throw new IllegalArgumentException("Session request cannot be null.");
    }

    String userId = normalizeRequired(request.getUserId(), "userId");
    List<String> courseIds = normalizeCourseIds(request.getCourseIds());

    return persistAndBuildSession(userId, courseIds, null, null);
  }

  /**
   * build a session from Canvas API data
   * uses the user's loginId as the userId and pulls course codes from each
     * course.
   *
   * @param user    the user profile we got from Canvas
   * @param courses the user's enrolled courses from Canvas
   * @return a SessionResponse built from the Canvas data
   */
  public SessionResponse fromCanvasData(CanvasUser user, List<CanvasCourse> courses, Map<Long, List<CanvasAssignment>> assignments) {
    String userId = normalizeRequired(user.getLoginId(), "userId");

    List<String> courseIds =
        courses.stream()
            .map(SessionService::canvasCourseToActiveId)
            .filter(Objects::nonNull)
            .distinct()
            .collect(Collectors.toList());

    return persistAndBuildSession(userId, normalizeCourseIds(courseIds), courses, assignments);
  }

  /**
   * Stable string used in {@code ooc_user.active_course_ids}: prefer Canvas course code, else a
   * synthetic id from the numeric Canvas course id.
   */
  private static String canvasCourseToActiveId(CanvasCourse c) {
    if (c == null) {
      return null;
    }
    String code = normalizeOptional(c.getCourseCode());
    if (code != null) {
      return code;
    }
    return "canvas-" + c.getId();
  }

  /**
   * Upserts the user's active session metadata and returns the API response.
   *
   * @param userId external user identifier
   * @param courseIds normalized list of active course ids
   * @return persisted session response payload
   */
  private SessionResponse persistAndBuildSession(
      String userId, List<String> courseIds, List<CanvasCourse> canvasCoursesForCache, Map<Long, List<CanvasAssignment>> assignments) {
    OocUser owner = oocUserRepository.findByExternalUserId(userId).orElseGet(OocUser::new);

    owner.setExternalUserId(userId);
    owner.setCurrentSessionId(UUID.randomUUID().toString());
    owner.setActiveCourseIds(String.join(",", courseIds));
    owner.setSessionCreatedAt(Instant.now());

    OocUser saved = oocUserRepository.save(owner);

    if (canvasCoursesForCache != null && !canvasCoursesForCache.isEmpty()) {
      courseCacheSyncService.replaceForUser(saved, canvasCoursesForCache, assignments);
    }

    return new SessionResponse(
        saved.getCurrentSessionId(),
        saved.getExternalUserId(),
        parseStoredCourseIds(saved.getActiveCourseIds()),
        saved.getSessionCreatedAt().toString());
  }

  /**
   * Converts stored comma-separated course ids into a list.
   *
   * @param serialized stored course-id string from the database
   * @return parsed course-id list, or empty list when null/blank
   */
  private static List<String> parseStoredCourseIds(String serialized) {
    if (serialized == null || serialized.isBlank()) {
      return List.of();
    }

    return Arrays.stream(serialized.split(","))
        .map(SessionService::normalizeOptional)
        .filter(value -> value != null)
        .toList();
  }

  /**
   * normalizes a required string field and throws if null/blank.
   */
  private static String normalizeRequired(String value, String fieldName) {
    String normalized = normalizeOptional(value);
    if (normalized == null) {
      throw new IllegalArgumentException(
          "Session " + fieldName + " cannot be null or blank.");
    }
    return normalized;
  }

  /**
   * trims an optional string field and converts blank values to null.
   */
  private static String normalizeOptional(String value) {
    if (value == null) {
      return null;
    }
    String trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }

  /**
   * normalizes course ids by trimming, dropping blanks, and removing duplicates.
   */
  private static List<String> normalizeCourseIds(List<String> courseIds) {
    if (courseIds == null || courseIds.isEmpty()) {
      throw new IllegalArgumentException("Session courseIds cannot be null or empty.");
    }

    List<String> normalized = courseIds.stream()
        .map(SessionService::normalizeOptional)
        .filter(value -> value != null)
        .distinct()
        .collect(Collectors.toList());

    if (normalized.isEmpty()) {
      throw new IllegalArgumentException("Session courseIds cannot be empty after normalization.");
    }

    return normalized;
  }
}
