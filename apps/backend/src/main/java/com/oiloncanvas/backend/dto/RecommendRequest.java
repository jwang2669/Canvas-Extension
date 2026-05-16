package com.oiloncanvas.backend.dto;

/**
 * Request body for POST /api/recommend.
 * Uses session context to generate next-task recommendations.
 */
public class RecommendRequest {

  /** Session identifier used to look up user context. */
  private String sessionId;

  public String getSessionId() { return sessionId; }
  public void setSessionId(String sessionId) { this.sessionId = sessionId; }
}
