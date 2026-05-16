package com.oiloncanvas.backend.dto;

/**
 * Task payload returned by task endpoints.
 * Used for both single-create response and weekly task lists.
 */
public class TaskResponse {

  /** Unique task id. */
  private long id;
  /** Task title. */
  private String title;
  /** Due date/time string. */
  private String dueDate;
  /** Task type (assignment/quiz/etc). */
  private String type;
  /** Estimated work time in minutes. */
  private int estimatedMinutes;
  /** Course code/name this task belongs to. */
  private String course;
  /** Task state (for example: pending, completed). */
  private String status;

  public TaskResponse() {}

  public TaskResponse(long id, String title, String dueDate, String type, int estimatedMinutes, String course, String status) {
    this.id = id;
    this.title = title;
    this.dueDate = dueDate;
    this.type = type;
    this.estimatedMinutes = estimatedMinutes;
    this.course = course;
    this.status = status;
  }

  public long getId() { return id; }
  public void setId(long id) { this.id = id; }
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
  public String getStatus() { return status; }
  public void setStatus(String status) { this.status = status; }
}
