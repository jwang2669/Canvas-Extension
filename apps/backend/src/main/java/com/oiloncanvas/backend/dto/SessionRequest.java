package com.oiloncanvas.backend.dto;

import java.util.List;

/**
 * Request body for POST /api/sessions.
 * Starts session context for one user and selected courses.
 */
public class SessionRequest {

  /** User identifier (currently sourced from Canvas login). */
  private String userId;
  /** Course identifiers/codes that should be active in the session. */
  private List<String> courseIds;

  public String getUserId() { return userId; }
  public void setUserId(String userId) { this.userId = userId; }
  public List<String> getCourseIds() { return courseIds; }
  public void setCourseIds(List<String> courseIds) { this.courseIds = courseIds; }
}
