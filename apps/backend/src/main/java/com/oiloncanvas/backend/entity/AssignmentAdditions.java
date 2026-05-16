package com.oiloncanvas.backend.entity;
import jakarta.persistence.*;

/**
 * Represents Oil on Canvas additions to an assignment
 */
@Entity
@Table(name = "assignment_additions")
public class AssignmentAdditions {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "addition_id")
    private Integer additionId;

    /**
     * Owning side: FK column {@code assignment_id} → {@link AssignmentCache}.
     * Inverse: {@link AssignmentCache#getAssignmentAdditions()}.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id", nullable = false)
    private AssignmentCache assignmentCache;

    @Column(name = "estimated_time")
    private Integer estimatedTime;

    @Column(name = "summary")
    private String summary;

    public Integer getAdditionId() {
        return additionId;
    }

    public AssignmentCache getAssignmentCache() {
        return assignmentCache;
    }

    public void setAssignmentCache(AssignmentCache assignmentCache) {
        this.assignmentCache = assignmentCache;
    }

    public Integer getEstimatedTime() {
        return estimatedTime;
    }

    public String getSummary() {
        return summary;
    }

    public void setEstimatedTime(Integer estimatedTime) {
        this.estimatedTime = estimatedTime;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }
}
