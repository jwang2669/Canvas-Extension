package com.oiloncanvas.backend.dto.canvasapi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * maps to the Canvas LMS Course object returned by 
 * GET /api/v1/courses.
 * https://canvas.instructure.com/doc/api/courses.html
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CanvasCourse {

    // only some fields are here, if need others, find them at the link

    /** The course's unique ID in Canvas. */
    private long id;

    /** Full course name (e.g. "Introduction to Linear Algebra"). */
    private String name;

    /** Short course code (e.g. "MATH340"). */
    @JsonProperty("course_code")
    private String courseCode;

    /** Which semester/term this course belongs to (an ID). */
    @JsonProperty("enrollment_term_id")
    private Long enrollmentTermId;

    /** When the course starts (date/time string). */
    @JsonProperty("start_at")
    private String startAt;

    /** When the course ends (date/time string). */
    @JsonProperty("end_at")
    private String endAt;

    /** The course's time zone, e.g. "America/Chicago". */
    @JsonProperty("time_zone")
    private String timeZone;

    /** Course lifecycle state, e.g. "available", "completed", "unpublished". */
    @JsonProperty("workflow_state")
    private String workflowState;

    public CanvasCourse() {}

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }

    public Long getEnrollmentTermId() { return enrollmentTermId; }
    public void setEnrollmentTermId(Long enrollmentTermId) { this.enrollmentTermId = enrollmentTermId; }

    public String getStartAt() { return startAt; }
    public void setStartAt(String startAt) { this.startAt = startAt; }

    public String getEndAt() { return endAt; }
    public void setEndAt(String endAt) { this.endAt = endAt; }

    public String getTimeZone() { return timeZone; }
    public void setTimeZone(String timeZone) { this.timeZone = timeZone; }

    public String getWorkflowState() { return workflowState; }
    public void setWorkflowState(String workflowState) { this.workflowState = workflowState; }
}
