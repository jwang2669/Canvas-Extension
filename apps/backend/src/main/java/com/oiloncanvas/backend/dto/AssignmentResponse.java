package com.oiloncanvas.backend.dto;

/**
 * DTO for a single assignment returned by GET /api/canvas/assignments.
 * Maps to TaskItem fields for workload scoring.
 */
public class AssignmentResponse {

    private Integer assignmentId;
    private Integer canvasAssignmentId;
    private String title;
    private String dueDate;
    private int estimatedMinutes;
    private String courseName;
    private Integer courseId;

    public AssignmentResponse() {}

    public AssignmentResponse(Integer assignmentId, Integer canvasAssignmentId, String title, 
                              String dueDate, int estimatedMinutes, String courseName, Integer courseId) {
        this.assignmentId = assignmentId;
        this.canvasAssignmentId = canvasAssignmentId;
        this.title = title;
        this.dueDate = dueDate;
        this.estimatedMinutes = estimatedMinutes;
        this.courseName = courseName;
        this.courseId = courseId;
    }

    public Integer getAssignmentId() {
        return assignmentId;
    }

    public void setAssignmentId(Integer assignmentId) {
        this.assignmentId = assignmentId;
    }

    public Integer getCanvasAssignmentId() {
        return canvasAssignmentId;
    }

    public void setCanvasAssignmentId(Integer canvasAssignmentId) {
        this.canvasAssignmentId = canvasAssignmentId;
    }

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

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public Integer getCourseId() {
        return courseId;
    }

    public void setCourseId(Integer courseId) {
        this.courseId = courseId;
    }
}
