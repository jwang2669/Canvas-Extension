package com.oiloncanvas.backend.dto;

/**
 * Request body for POST /api/analyze.
 * Contains the week being analyzed and optional free-form content.
 */
public class AnalyzeRequest {

  /** Week identifier (for example, "2026-W10"). */
  private String week;
  /** Additional notes/content to analyze (optional). */
  private String content;

  public String getWeek() { return week; }
  public void setWeek(String week) { this.week = week; }
  public String getContent() { return content; }
  public void setContent(String content) { this.content = content; }
}
