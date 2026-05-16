package com.oiloncanvas.backend.entity;
import java.time.LocalDateTime;
import jakarta.persistence.*;

/**
 * Represents data we cache for each assignment an Oil on Canvas user has
 */
@Entity
@Table(name = "assignment_cache")
public class AssignmentCache {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "assignment_id")
    private Integer assignmentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private CourseCache courseCache;

    @Column(name = "canvas_assignment_id", nullable = false)
    private Integer canvasAssignmentId;

    @Column(name = "assignment_name", nullable = false)
    private String assignmentName;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @OneToOne(mappedBy = "assignmentCache", fetch = FetchType.LAZY)
    private AssignmentAdditions assignmentAdditions;

    public Integer getAssignmentId() {
        return assignmentId;
    }

    public CourseCache getCourseCache() {
        return courseCache;
    }

    public void setCourseCache(CourseCache courseCache) {
        this.courseCache = courseCache;
    }

    public Integer getCanvasAssignmentId() {
        return canvasAssignmentId;
    }

    public void setCanvasAssignmentId(Integer canvasAssignmentId) {
        this.canvasAssignmentId = canvasAssignmentId;
    }

    public String getAssignmentName() {
        return assignmentName;
    }

    public void setAssignmentName(String assignmentName) {
        this.assignmentName = assignmentName;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }

    public AssignmentAdditions getAssignmentAdditions() {
        return assignmentAdditions;
    }

    public void setAssignmentAdditions(AssignmentAdditions assignmentAdditions) {
        this.assignmentAdditions = assignmentAdditions;
    }
}
