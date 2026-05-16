package com.oiloncanvas.backend.dto;

import com.oiloncanvas.backend.dto.canvasapi.CanvasCourse;
import java.util.List;

/**
 * API response for {@code GET /api/canvas/courses}: Canvas courses the token holder is actively
 * enrolled in (enrollment_state=active), after filtering incomplete or unavailable course stubs.
 */
public class EnrolledCoursesResponse {

  private final List<CanvasCourse> courses;
  /** Populated when the server persists session + course cache for the Canvas token holder. */
  private String sessionId;
  /** External user id (Canvas login id) aligned with {@code ooc_user.external_user_id}. */
  private String userId;

  public EnrolledCoursesResponse(List<CanvasCourse> courses) {
    this.courses = courses;
  }

  public List<CanvasCourse> getCourses() {
    return courses;
  }

  public String getSessionId() {
    return sessionId;
  }

  public void setSessionId(String sessionId) {
    this.sessionId = sessionId;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }
}
