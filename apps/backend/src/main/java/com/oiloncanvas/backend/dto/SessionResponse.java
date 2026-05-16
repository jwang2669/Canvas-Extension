package com.oiloncanvas.backend.dto;

import java.util.List;

/**
 * Response body for POST /api/sessions.
 * Contains IDs and timing metadata for the active session.
 */
public class SessionResponse {

  /** Unique session id. */
  private String sessionId;
  /** User id associated with this session. */
  private String userId;
  /** Active course ids/codes in this session. */
  private List<String> courseIds;
  /** Session creation time (ISO-8601 string). */
  private String createdAt;

  public SessionResponse() {}

  public SessionResponse(String sessionId, String userId, List<String> courseIds, String createdAt) {
    this.sessionId = sessionId;
    this.userId = userId;
    this.courseIds = courseIds;
    this.createdAt = createdAt;
  }

  public String getSessionId() { return sessionId; }
  public void setSessionId(String sessionId) { this.sessionId = sessionId; }
  public String getUserId() { return userId; }
  public void setUserId(String userId) { this.userId = userId; }
  public List<String> getCourseIds() { return courseIds; }
  public void setCourseIds(List<String> courseIds) { this.courseIds = courseIds; }
  public String getCreatedAt() { return createdAt; }
  public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
