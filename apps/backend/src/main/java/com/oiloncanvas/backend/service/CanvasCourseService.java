package com.oiloncanvas.backend.service;

import com.oiloncanvas.backend.client.CanvasApiClient;
import com.oiloncanvas.backend.dto.AssignmentResponse;
import com.oiloncanvas.backend.dto.AssignmentsResponse;
import com.oiloncanvas.backend.dto.CanvasUserProfileResponse;
import com.oiloncanvas.backend.dto.EnrolledCoursesResponse;
import com.oiloncanvas.backend.dto.SessionResponse;
import com.oiloncanvas.backend.dto.canvasapi.CanvasAssignment;
import com.oiloncanvas.backend.dto.canvasapi.CanvasCourse;
import com.oiloncanvas.backend.dto.canvasapi.CanvasUser;
import com.oiloncanvas.backend.entity.AssignmentAdditions;
import com.oiloncanvas.backend.entity.AssignmentCache;
import com.oiloncanvas.backend.repository.AssignmentCacheRepository;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Fetches Canvas course data using {@link CanvasApiClient} and applies filtering for API responses.
 */
@Service
public class CanvasCourseService {

  private static final int DEFAULT_ESTIMATED_MINUTES = 60;

  private final Optional<CanvasApiClient> canvasApiClient;
  private final SessionService sessionService;
  private final AssignmentCacheRepository assignmentCacheRepository;

  public CanvasCourseService(
      Optional<CanvasApiClient> canvasApiClient,
      SessionService sessionService,
      AssignmentCacheRepository assignmentCacheRepository) {
    this.canvasApiClient = canvasApiClient;
    this.sessionService = sessionService;
    this.assignmentCacheRepository = assignmentCacheRepository;
  }

  /**
   * Returns courses where the user has an active Canvas enrollment, excluding stub entries (e.g.
   * only {@code id} + {@code access_restricted_by_date}) and non-available workflow states when
   * {@code workflow_state} is present.
   *
   * @throws ResponseStatusException {@code 503} if Canvas env vars are not set (no client bean)
   */
  public EnrolledCoursesResponse getCurrentlyEnrolledCourses() {
    CanvasApiClient client = requireCanvasClient();

    List<CanvasCourse> raw = client.getCoursesWithActiveEnrollment();
    List<CanvasCourse> courses =
        raw.stream()
            .filter(c -> c.getName() != null && !c.getName().isBlank())
            .filter(
                c ->
                    c.getWorkflowState() == null
                        || "available".equalsIgnoreCase(c.getWorkflowState()))
            .collect(Collectors.toList());
    Map<Long, List<CanvasAssignment>> assignmentsByCourse = new HashMap<>();
    for (CanvasCourse course : courses) {
      List<CanvasAssignment> assignments = client.getAssignmentsForCourse((int) course.getId());
      assignmentsByCourse.put(course.getId(), assignments);
    }
    EnrolledCoursesResponse response = new EnrolledCoursesResponse(courses);
    CanvasUser user = client.getCurrentUser();
    if (!courses.isEmpty()) {
      CanvasUser sessionUser = user;
      if (sessionUser == null
          || sessionUser.getLoginId() == null
          || sessionUser.getLoginId().isBlank()) {
        // Some Canvas tokens can list courses but not return /users/self.login_id.
        // For now we still want a stable per-token session + DB cache.
        sessionUser = new CanvasUser();
        sessionUser.setLoginId("canvas-self");
      }
      try {
        SessionResponse session = sessionService.fromCanvasData(sessionUser, courses, assignmentsByCourse);
        response.setSessionId(session.getSessionId());
        response.setUserId(session.getUserId());
      } catch (IllegalArgumentException ignored) {
        // Leave sessionId unset if Canvas user or course list cannot be normalized.
      }
    }
    return response;
  }

  /**
   * Returns the Canvas user profile for the configured token ({@code GET /api/v1/users/self}).
   *
   * @throws ResponseStatusException {@code 503} if Canvas env vars are not set; {@code 502} if
   *     Canvas does not return a profile (invalid token, wrong base URL, or network/API error).
   */
  public CanvasUserProfileResponse getCurrentUserProfile() {
    CanvasApiClient client = requireCanvasClient();
    CanvasUser user = client.getCurrentUser();
    if (user == null) {
      throw new ResponseStatusException(
          HttpStatus.BAD_GATEWAY,
          "Canvas did not return a user profile. Check CANVAS_BASE_URL, CANVAS_ACCESS_TOKEN, and"
              + " network access.");
    }
    return new CanvasUserProfileResponse(user);
  }

  /**
   * Returns cached assignments for the specified week.
   * week=current: assignments due Mon-Sun of current ISO week
   * week=next: assignments due Mon-Sun of next ISO week
   * No param: assignments due today through 7 days from now
   */
  public AssignmentsResponse getAssignmentsForWeek(String week) {
    LocalDate today = LocalDate.now();
    LocalDate startDate;
    LocalDate endDate;

    if ("current".equalsIgnoreCase(week)) {
      startDate = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
      endDate = startDate.plusDays(6);
    } else if ("next".equalsIgnoreCase(week)) {
      startDate = today.with(TemporalAdjusters.next(DayOfWeek.MONDAY));
      endDate = startDate.plusDays(6);
    } else {
      startDate = today;
      endDate = today.plusDays(7);
    }

    LocalDateTime startDateTime = startDate.atStartOfDay();
    LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

    List<AssignmentCache> cached = assignmentCacheRepository.findByDueDateBetween(startDateTime, endDateTime);

    List<AssignmentResponse> assignments = cached.stream()
        .map(this::toAssignmentResponse)
        .collect(Collectors.toList());

    return new AssignmentsResponse(assignments, startDate.toString(), endDate.toString());
  }

  private AssignmentResponse toAssignmentResponse(AssignmentCache cache) {
    String dueDate = cache.getDueDate() != null
        ? cache.getDueDate().toLocalDate().toString()
        : null;

    int estimatedMinutes = DEFAULT_ESTIMATED_MINUTES;
    AssignmentAdditions additions = cache.getAssignmentAdditions();
    if (additions != null && additions.getEstimatedTime() != null) {
      estimatedMinutes = additions.getEstimatedTime();
    }

    String courseName = cache.getCourseCache() != null
        ? cache.getCourseCache().getCourseName()
        : "Unknown Course";

    Integer courseId = cache.getCourseCache() != null
        ? cache.getCourseCache().getCourseId()
        : null;

    return new AssignmentResponse(
        cache.getAssignmentId(),
        cache.getCanvasAssignmentId(),
        cache.getAssignmentName(),
        dueDate,
        estimatedMinutes,
        courseName,
        courseId
    );
  }

  private CanvasApiClient requireCanvasClient() {
    return canvasApiClient.orElseThrow(
        () ->
            new ResponseStatusException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Canvas is not configured. Set CANVAS_BASE_URL and CANVAS_ACCESS_TOKEN in the"
                    + " environment (e.g. infra/.env when using run.sh)."));
  }

  @Scheduled(fixedRateString = "${cache.refresh.rate.ms:43200000}")
  public void scheduledCourseAndAssignmentRefresh() {
    try {
      getCurrentlyEnrolledCourses();
    } catch (ResponseStatusException ignored) {

    }
  }
}
