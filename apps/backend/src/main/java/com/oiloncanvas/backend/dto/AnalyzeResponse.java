package com.oiloncanvas.backend.dto;

import java.util.List;

/**
 * Response body for POST /api/analyze.
 * Returns total workload, heavy days, and a short summary.
 */
public class AnalyzeResponse {

  /** Aggregate workload score for the requested period. */
  private int totalLoad;
  /** Day names considered heavy (for example, Monday/Thursday). */
  private List<String> heavyDays;
  /** Human-readable summary/explanation of workload. */
  private String summary;

  public AnalyzeResponse() {}

  public AnalyzeResponse(int totalLoad, List<String> heavyDays, String summary) {
    this.totalLoad = totalLoad;
    this.heavyDays = heavyDays;
    this.summary = summary;
  }

  public int getTotalLoad() { return totalLoad; }
  public void setTotalLoad(int totalLoad) { this.totalLoad = totalLoad; }
  public List<String> getHeavyDays() { return heavyDays; }
  public void setHeavyDays(List<String> heavyDays) { this.heavyDays = heavyDays; }
  public String getSummary() { return summary; }
  public void setSummary(String summary) { this.summary = summary; }
}
