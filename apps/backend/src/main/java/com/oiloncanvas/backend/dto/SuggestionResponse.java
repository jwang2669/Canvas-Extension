package com.oiloncanvas.backend.dto;

/**
 * Response body for POST /api/suggestions — AI-generated recommendation text.
 */
public class SuggestionResponse {

  private final String recommendation;

  public SuggestionResponse(String recommendation) {
    this.recommendation = recommendation;
  }

  public String getRecommendation() {
    return recommendation;
  }
}
