package com.oiloncanvas.backend.dto;

import java.util.List;

/**
 * Request body for POST /api/workload — workload scoring and AI summary for a set of tasks.
 * referenceDate: optional ISO date (e.g. "2026-03-02"); if missing, today is used for due-date proximity.
 */
public class WorkloadRequest {

  /** Tasks to score for this request. */
  private List<TaskItem> tasks;
  /** Optional week label (for display/context), e.g. "2026-W10". */
  private String week;
  /** Optional scoring reference date (ISO yyyy-MM-dd). */
  private String referenceDate;

  public List<TaskItem> getTasks() {
    return tasks;
  }

  public void setTasks(List<TaskItem> tasks) {
    this.tasks = tasks;
  }

  public String getWeek() {
    return week;
  }

  public void setWeek(String week) {
    this.week = week;
  }

  public String getReferenceDate() {
    return referenceDate;
  }

  public void setReferenceDate(String referenceDate) {
    this.referenceDate = referenceDate;
  }
}
