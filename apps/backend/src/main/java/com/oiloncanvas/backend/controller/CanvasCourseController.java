package com.oiloncanvas.backend.controller;

import com.oiloncanvas.backend.dto.AssignmentsResponse;
import com.oiloncanvas.backend.dto.CanvasUserProfileResponse;
import com.oiloncanvas.backend.dto.EnrolledCoursesResponse;
import com.oiloncanvas.backend.service.CanvasCourseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST API for Canvas course data (server-side token).
 */
@RestController
@RequestMapping("/api/canvas")
public class CanvasCourseController {

  private final CanvasCourseService canvasCourseService;

  public CanvasCourseController(CanvasCourseService canvasCourseService) {
    this.canvasCourseService = canvasCourseService;
  }

  /**
   * Lists courses the configured Canvas token can see with {@code enrollment_state=active},
   * excluding incomplete course objects and non-available courses when {@code workflow_state} is
   * set.
   */
  @GetMapping("/courses")
  public ResponseEntity<EnrolledCoursesResponse> getCurrentlyEnrolledCourses() {
    return ResponseEntity.ok(canvasCourseService.getCurrentlyEnrolledCourses());
  }

  /**
   * Profile for the token holder ({@code GET /api/v1/users/self} on Canvas).
   * Aliases: {@code /me} and {@code /user} (same handler).
   */
  @GetMapping(value = {"/me", "/user"})
  public ResponseEntity<CanvasUserProfileResponse> getCurrentCanvasUser() {
    return ResponseEntity.ok(canvasCourseService.getCurrentUserProfile());
  }

  /**
   * Returns cached assignments for the specified week.
   * Query params:
   *   week=current - assignments due Mon-Sun of current ISO week
   *   week=next - assignments due Mon-Sun of next ISO week
   *   No param - assignments due today through 7 days from now
   */
  @GetMapping("/assignments")
  public ResponseEntity<AssignmentsResponse> getAssignments(
      @RequestParam(value = "week", required = false) String week) {
    return ResponseEntity.ok(canvasCourseService.getAssignmentsForWeek(week));
  }
}
