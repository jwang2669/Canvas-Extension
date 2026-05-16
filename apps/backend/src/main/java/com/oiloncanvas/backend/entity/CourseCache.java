package com.oiloncanvas.backend.entity;
import java.util.List;
import jakarta.persistence.*;

/**
 * Represents course data we cache for each course an Oil on Canvas user is enrolled in
 */
@Entity
@Table(name = "course_cache")
public class CourseCache {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "course_id")
    private Integer courseId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ooc_id", nullable = false)
    private OocUser oocUser;

    @Column(name = "canvas_course_id", nullable = false)
    private Integer canvasCourseId;

    @Column(name = "course_name", nullable = false)
    private String courseName;

    @Column(name = "course_code", nullable = false)
    private String courseCode;

    @OneToMany(mappedBy = "courseCache", fetch = FetchType.LAZY)
    private List<AssignmentCache> assignmentCaches;

    public Integer getCourseId() {
        return courseId;
    }

    public OocUser getOocUser() {
        return oocUser;
    }

    public void setOocUser(OocUser oocUser) {
        this.oocUser = oocUser;
    }

    public Integer getCanvasCourseId() {
        return canvasCourseId;
    }

    public void setCanvasCourseId(Integer canvasCourseId) {
        this.canvasCourseId = canvasCourseId;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public List<AssignmentCache> getAssignmentCaches() {
        return assignmentCaches;
    }

    public void setAssignmentCaches(List<AssignmentCache> assignmentCaches) {
        this.assignmentCaches = assignmentCaches;
    }
}
