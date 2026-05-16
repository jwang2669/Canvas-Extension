package com.oiloncanvas.backend.database;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;

public class AssignmentCacheTest extends DatabaseTest {

    private int courseId;

    @BeforeEach
    void setup() throws SQLException {
        int oocId = insertUser();
        courseId  = insertCourse(oocId, 200, "Biology", "BIO101");
    }

    @Test
    void insertAssignment_withDueDate() throws SQLException {
        Timestamp due          = Timestamp.valueOf("2025-05-01 23:59:00");
        int       assignmentId = insertAssignment(courseId, 5001, "Lab Report", due);

        PreparedStatement query = connection.prepareStatement(
            "SELECT * FROM assignment_cache WHERE assignment_id = ?"
        );
        query.setInt(1, assignmentId);
        ResultSet rs = query.executeQuery();

        assertTrue(rs.next());
        assertEquals(courseId,     rs.getInt("course_id"));
        assertEquals(5001,         rs.getInt("canvas_assignment_id"));
        assertEquals("Lab Report", rs.getString("assignment_name"));
        assertEquals(due,          rs.getTimestamp("due_date"));
    }

    @Test
    void insertAssignment_nullDueDate_allowed() {
        assertDoesNotThrow(() -> insertAssignment(courseId, 5002, "Optional Reading", null));
    }

    @Test
    void insertAssignment_nullName_rejected() {
        assertThrows(SQLException.class, () -> {
            PreparedStatement stmt = connection.prepareStatement(
                "INSERT INTO assignment_cache (canvas_assignment_id, course_id, assignment_name, due_date) VALUES (?, ?, ?, ?)"
            );
            stmt.setInt(1, 5003);
            stmt.setInt(2, courseId);
            stmt.setNull(3, Types.VARCHAR);
            stmt.setNull(4, Types.TIMESTAMP);
            stmt.executeUpdate();
        });
    }

    @Test
    void insertAssignment_invalidCourseId_rejected() {
        assertThrows(SQLException.class, () ->
            insertAssignment(99999, 5004, "Ghost Assignment", null)
        );
    }
}