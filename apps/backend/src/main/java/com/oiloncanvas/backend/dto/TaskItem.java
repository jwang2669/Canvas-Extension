package com.oiloncanvas.backend.dto;

/**
 * A single task used for workload scoring (e.g. in WorkloadRequest).
 * dueDate: ISO local date (e.g. "2026-03-05").
 */
public class TaskItem {

  /** Task title shown in workload output/summaries. */
  private String title;
  /** Due date in ISO local-date format (yyyy-MM-dd). */
  private String dueDate;
  /** Estimated work time in minutes. */
  private int estimatedMinutes;

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getDueDate() {
    return dueDate;
  }

  public void setDueDate(String dueDate) {
    this.dueDate = dueDate;
  }

  public int getEstimatedMinutes() {
    return estimatedMinutes;
  }

  public void setEstimatedMinutes(int estimatedMinutes) {
    this.estimatedMinutes = estimatedMinutes;
  }
}
