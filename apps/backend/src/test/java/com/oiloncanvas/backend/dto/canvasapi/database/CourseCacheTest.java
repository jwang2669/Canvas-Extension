package com.oiloncanvas.backend.database;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;

public class CourseCacheTest extends DatabaseTest {

    private int oocId;

    @BeforeEach
    void setup() throws SQLException {
        oocId = insertUser();
    }

    @Test
    void insertCourse_storesAllFields() throws SQLException {
        int courseId = insertCourse(oocId, 9001, "Intro to CS", "CS101");

        PreparedStatement query = connection.prepareStatement(
            "SELECT * FROM course_cache WHERE course_id = ?"
        );
        query.setInt(1, courseId);
        ResultSet rs = query.executeQuery();

        assertTrue(rs.next());
        assertEquals(oocId,          rs.getInt("ooc_id"));
        assertEquals(9001,           rs.getInt("canvas_course_id"));
        assertEquals("Intro to CS", rs.getString("course_name"));
        assertEquals("CS101",        rs.getString("course_code"));
    }

    @Test
    void insertCourse_invalidOocId_rejected() {
        assertThrows(SQLException.class, () ->
            insertCourse(99999, 101, "Ghost Course", "GHOST101")
        );
    }

    @Test
    void insertCourse_nullCourseName_rejected() {
        assertThrows(SQLException.class, () -> {
            PreparedStatement stmt = connection.prepareStatement(
                "INSERT INTO course_cache (ooc_id, canvas_course_id, course_name, course_code) VALUES (?, ?, ?, ?)"
            );
            stmt.setInt(1, oocId);
            stmt.setInt(2, 101);
            stmt.setNull(3, Types.VARCHAR);
            stmt.setString(4, "CS101");
            stmt.executeUpdate();
        });
    }

    @Test
    void insertCourse_nullCourseCode_rejected() {
        assertThrows(SQLException.class, () -> {
            PreparedStatement stmt = connection.prepareStatement(
                "INSERT INTO course_cache (ooc_id, canvas_course_id, course_name, course_code) VALUES (?, ?, ?, ?)"
            );
            stmt.setInt(1, oocId);
            stmt.setInt(2, 101);
            stmt.setString(3, "Intro to CS");
            stmt.setNull(4, Types.VARCHAR);
            stmt.executeUpdate();
        });
    }

    @Test
    void courses_isolatedByUser() throws SQLException {
        int otherOocId = insertUser();
        insertCourse(oocId,      101, "Math",    "MATH101");
        insertCourse(otherOocId, 102, "Physics", "PHYS101");

        PreparedStatement query = connection.prepareStatement(
            "SELECT COUNT(*) FROM course_cache WHERE ooc_id = ?"
        );
        query.setInt(1, oocId);
        ResultSet rs = query.executeQuery();
        rs.next();

        assertEquals(1, rs.getInt(1));
    }
}