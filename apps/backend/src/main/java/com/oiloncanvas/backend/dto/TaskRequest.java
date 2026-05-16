package com.oiloncanvas.backend.dto;

/**
 * Request body for POST /api/tasks.
 * Represents one task the user wants to create.
 */
public class TaskRequest {

  /** Task title shown to the user. */
  private String title;
  /** Due date/time string. */
  private String dueDate;
  /** Task type (for example: assignment, quiz, exam). */
  private String type;
  /** Estimated work time in minutes. */
  private int estimatedMinutes;
  /** Course code/name this task belongs to. */
  private String course;

  public String getTitle() { return title; }
  public void setTitle(String title) { this.title = title; }
  public String getDueDate() { return dueDate; }
  public void setDueDate(String dueDate) { this.dueDate = dueDate; }
  public String getType() { return type; }
  public void setType(String type) { this.type = type; }
  public int getEstimatedMinutes() { return estimatedMinutes; }
  public void setEstimatedMinutes(int estimatedMinutes) { this.estimatedMinutes = estimatedMinutes; }
  public String getCourse() { return course; }
  public void setCourse(String course) { this.course = course; }
}
