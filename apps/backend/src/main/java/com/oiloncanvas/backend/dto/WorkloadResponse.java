package com.oiloncanvas.backend.dto;

import java.util.Map;

/**
 * Response for POST /api/workload: per-day and total minutes, weighted scores, and AI summary.
 * perDayMinutes: raw estimated minutes per day of week (e.g. "Monday", "Tuesday").
 * weightedPerDay: due-date weighted score per day (tasks due sooner count more).
 */
public class WorkloadResponse {

  /** Raw estimated minutes per weekday. */
  private Map<String, Integer> perDayMinutes;
  /** Sum of estimated minutes across all valid tasks. */
  private int totalMinutes;
  /** Urgency-weighted score per weekday (higher means more urgent load). */
  private Map<String, Double> weightedPerDay;
  /** AI-generated short summary for the student. */
  private String summary;

  public WorkloadResponse() {}

  public WorkloadResponse(
      Map<String, Integer> perDayMinutes,
      int totalMinutes,
      Map<String, Double> weightedPerDay,
      String summary) {
    this.perDayMinutes = perDayMinutes;
    this.totalMinutes = totalMinutes;
    this.weightedPerDay = weightedPerDay;
    this.summary = summary;
  }

  public Map<String, Integer> getPerDayMinutes() {
    return perDayMinutes;
  }

  public void setPerDayMinutes(Map<String, Integer> perDayMinutes) {
    this.perDayMinutes = perDayMinutes;
  }

  public int getTotalMinutes() {
    return totalMinutes;
  }

  public void setTotalMinutes(int totalMinutes) {
    this.totalMinutes = totalMinutes;
  }

  public Map<String, Double> getWeightedPerDay() {
    return weightedPerDay;
  }

  public void setWeightedPerDay(Map<String, Double> weightedPerDay) {
    this.weightedPerDay = weightedPerDay;
  }

  public String getSummary() {
    return summary;
  }

  public void setSummary(String summary) {
    this.summary = summary;
  }
}
