package com.oiloncanvas.backend.entity;
import java.time.Instant;
import java.util.List;
import jakarta.persistence.*;

/**
 * Represents an Oil on Canvas user
 */
@Entity
@Table(name = "ooc_user")
public class OocUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ooc_id")
    private Integer oocId;

    @Column(name = "external_user_id")
    private String externalUserId;

    @Column(name = "current_session_id")
    private String currentSessionId;

    @Column(name = "active_course_ids")
    private String activeCourseIds;

    @Column(name = "session_created_at")
    private Instant sessionCreatedAt;

    @OneToMany(mappedBy = "oocUser", fetch = FetchType.LAZY)
    private List<CanvasConnection> canvasConnections;

    @OneToMany(mappedBy = "oocUser", fetch = FetchType.LAZY)
    private List<CourseCache> courseCaches;

    @OneToMany(mappedBy = "oocUser", fetch = FetchType.LAZY)
    private List<TodoItem> todoItems;

    public Integer getOocId() {
        return oocId;
    }

    /**
     * Gets the external user identifier associated with this OOC user.
     */
    public String getExternalUserId() {
        return externalUserId;
    }

    /**
     * Sets the external user identifier associated with this OOC user.
     */
    public void setExternalUserId(String externalUserId) {
        this.externalUserId = externalUserId;
    }

    /**
     * Gets the current active session id for this user.
     */
    public String getCurrentSessionId() {
        return currentSessionId;
    }

    /**
     * Sets the current active session id for this user.
     */
    public void setCurrentSessionId(String currentSessionId) {
        this.currentSessionId = currentSessionId;
    }

    /**
     * Gets serialized active course ids for this user's session.
     */
    public String getActiveCourseIds() {
        return activeCourseIds;
    }

    /**
     * Sets serialized active course ids for this user's session.
     */
    public void setActiveCourseIds(String activeCourseIds) {
        this.activeCourseIds = activeCourseIds;
    }

    /**
     * Gets the timestamp when the user's current session was created.
     */
    public Instant getSessionCreatedAt() {
        return sessionCreatedAt;
    }

    /**
     * Sets the timestamp when the user's current session was created.
     */
    public void setSessionCreatedAt(Instant sessionCreatedAt) {
        this.sessionCreatedAt = sessionCreatedAt;
    }

    public List<CanvasConnection> getCanvasConnections() {
        return canvasConnections;
    }

    public void setCanvasConnections(List<CanvasConnection> canvasConnections) {
        this.canvasConnections = canvasConnections;
    }

    public List<CourseCache> getCourseCaches() {
        return courseCaches;
    }

    public void setCourseCaches(List<CourseCache> courseCaches){
        this.courseCaches = courseCaches;
    }
    
    public List<TodoItem> getTodoItems() {
        return todoItems;
    }

    public void setTodoItems(List<TodoItem> todoItems) {
        this.todoItems = todoItems;
    }
}
