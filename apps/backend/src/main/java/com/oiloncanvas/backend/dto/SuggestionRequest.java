package com.oiloncanvas.backend.dto;

/** Request body for POST /api/suggestions — prompt for AI suggestion. */
public class SuggestionRequest {

  private String prompt;
  /**
   * Session id from {@code GET /api/canvas/courses} (or {@code POST /api/sessions}). When set,
   * the server loads cached courses and assignments for personalized answers.
   */
  private String sessionId;

  /** Optional display name from Canvas profile (client may send from {@code GET /api/canvas/me}). */
  private String userDisplayName;

  public String getPrompt() {
    return prompt;
  }

  public void setPrompt(String prompt) {
    this.prompt = prompt;
  }

  public String getSessionId() {
    return sessionId;
  }

  public void setSessionId(String sessionId) {
    this.sessionId = sessionId;
  }

  public String getUserDisplayName() {
    return userDisplayName;
  }

  public void setUserDisplayName(String userDisplayName) {
    this.userDisplayName = userDisplayName;
  }
}
