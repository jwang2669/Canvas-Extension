package com.oiloncanvas.backend.dto;

import java.util.List;

/**
 * Response body for POST /api/recommend.
 * Includes one recommendation plus optional suggested task list.
 */
public class RecommendResponse {

  /** Primary recommendation text. */
  private String recommendation;
  /** Optional task titles the user should consider doing next. */
  private List<String> suggestedTasks;

  public RecommendResponse() {}

  public RecommendResponse(String recommendation, List<String> suggestedTasks) {
    this.recommendation = recommendation;
    this.suggestedTasks = suggestedTasks;
  }

  public String getRecommendation() { return recommendation; }
  public void setRecommendation(String recommendation) { this.recommendation = recommendation; }
  public List<String> getSuggestedTasks() { return suggestedTasks; }
  public void setSuggestedTasks(List<String> suggestedTasks) { this.suggestedTasks = suggestedTasks; }
}
