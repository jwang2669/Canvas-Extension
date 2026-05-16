package com.oiloncanvas.backend.dto.canvasapi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * maps to the Canvas LMS Assignment object returned by
 * GET /api/v1/courses/:course_id/assignments.
 * https://canvas.instructure.com/doc/api/assignments.html
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CanvasAssignment {

    // only some fields are here, if need others, find them at the link

    /** The assignment's unique ID in Canvas. */
    private long id;

    /** Human-readable assignment title. */
    private String name;

    /** HTML description of the assignment. */
    private String description;

    /** When the assignment is due (date/time string like "2026-03-14T23:59:00Z"). */
    @JsonProperty("due_at")
    private String dueAt;

    /** After this date/time, students can no longer submit. */
    @JsonProperty("lock_at")
    private String lockAt;

    /** When the assignment becomes available to students. */
    @JsonProperty("unlock_at")
    private String unlockAt;

    /** The ID of the course this assignment belongs to. */
    @JsonProperty("course_id")
    private long courseId;

    /** Max points possible for this assignment. */
    @JsonProperty("points_possible")
    private Double pointsPossible;

    /** How students can submit, e.g. "online_upload", "online_text_entry". */
    @JsonProperty("submission_types")
    private List<String> submissionTypes;

    /** True if any student has already submitted to this assignment. */
    @JsonProperty("has_submitted_submissions")
    private Boolean hasSubmittedSubmissions;

    /** Link to the assignment page in Canvas. */
    @JsonProperty("html_url")
    private String htmlUrl;

    public CanvasAssignment() {}

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getDueAt() { return dueAt; }
    public void setDueAt(String dueAt) { this.dueAt = dueAt; }

    public String getLockAt() { return lockAt; }
    public void setLockAt(String lockAt) { this.lockAt = lockAt; }

    public String getUnlockAt() { return unlockAt; }
    public void setUnlockAt(String unlockAt) { this.unlockAt = unlockAt; }

    public long getCourseId() { return courseId; }
    public void setCourseId(long courseId) { this.courseId = courseId; }

    public Double getPointsPossible() { return pointsPossible; }
    public void setPointsPossible(Double pointsPossible) { this.pointsPossible = pointsPossible; }

    public List<String> getSubmissionTypes() { return submissionTypes; }
    public void setSubmissionTypes(List<String> submissionTypes) { this.submissionTypes = submissionTypes; }

    public Boolean getHasSubmittedSubmissions() { return hasSubmittedSubmissions; }
    public void setHasSubmittedSubmissions(Boolean hasSubmittedSubmissions) { this.hasSubmittedSubmissions = hasSubmittedSubmissions; }

    public String getHtmlUrl() { return htmlUrl; }
    public void setHtmlUrl(String htmlUrl) { this.htmlUrl = htmlUrl; }
}
