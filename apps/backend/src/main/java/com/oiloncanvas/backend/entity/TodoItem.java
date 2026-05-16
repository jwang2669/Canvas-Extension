package com.oiloncanvas.backend.entity;
import java.time.LocalDateTime;
import jakarta.persistence.*;

/**
 * Represents To-do item for Oil on Canvas user
 */
@Entity
@Table(name = "todo_item")
public class TodoItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "todo_id")
    private Integer todoId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ooc_id", nullable = false)
    private OocUser oocUser;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "task_type")
    private String taskType;

    @Column(name = "estimated_minutes")
    private Integer estimatedMinutes;

    @Column(name = "course_code")
    private String courseCode;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @Column(name = "completed")
    private Boolean completed = false;

    public Integer getTodoId() {
        return todoId;
    }

    public OocUser getOocUser() {
        return oocUser;
    }

    public void setOocUser(OocUser oocUser) {
        this.oocUser = oocUser;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }

    public Boolean getCompleted() {
        return completed;
    }

    public void setCompleted(Boolean completed) {
        this.completed = completed;
    }

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    public Integer getEstimatedMinutes() {
        return estimatedMinutes;
    }

    public void setEstimatedMinutes(Integer estimatedMinutes) {
        this.estimatedMinutes = estimatedMinutes;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }
}
