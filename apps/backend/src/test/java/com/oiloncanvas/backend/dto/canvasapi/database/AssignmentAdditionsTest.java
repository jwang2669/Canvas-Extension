package com.oiloncanvas.backend.database;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;

public class AssignmentAdditionsTest extends DatabaseTest {

    private int assignmentId;

    @BeforeEach
    void setup() throws SQLException {
        int oocId        = insertUser();
        int courseId     = insertCourse(oocId, 200, "CS", "CS101");
        assignmentId     = insertAssignment(courseId, 5001, "Project 1", null);
    }

    @Test
    void insertAddition_allFields() throws SQLException {
        PreparedStatement stmt = connection.prepareStatement(
            "INSERT INTO assignment_additions (assignment_id, estimated_time, summary) VALUES (?, ?, ?)",
            Statement.RETURN_GENERATED_KEYS
        );
        stmt.setInt(1, assignmentId);
        stmt.setInt(2, 120);
        stmt.setString(3, "Build a REST API.");
        stmt.executeUpdate();

        PreparedStatement query = connection.prepareStatement(
            "SELECT * FROM assignment_additions WHERE assignment_id = ?"
        );
        query.setInt(1, assignmentId);
        ResultSet rs = query.executeQuery();

        assertTrue(rs.next());
        assertEquals(assignmentId,     rs.getInt("assignment_id"));
        assertEquals(120,              rs.getInt("estimated_time"));
        assertEquals("Build a REST API.", rs.getString("summary"));
    }

    @Test
    void insertAddition_nullEstimatedTime_allowed() {
        assertDoesNotThrow(() -> {
            PreparedStatement stmt = connection.prepareStatement(
                "INSERT INTO assignment_additions (assignment_id, estimated_time, summary) VALUES (?, ?, ?)"
            );
            stmt.setInt(1, assignmentId);
            stmt.setNull(2, Types.INTEGER);
            stmt.setString(3, "Summary only.");
            stmt.executeUpdate();
        });
    }

    @Test
    void insertAddition_nullSummary_allowed() {
        assertDoesNotThrow(() -> {
            PreparedStatement stmt = connection.prepareStatement(
                "INSERT INTO assignment_additions (assignment_id, estimated_time, summary) VALUES (?, ?, ?)"
            );
            stmt.setInt(1, assignmentId);
            stmt.setInt(2, 45);
            stmt.setNull(3, Types.CLOB);
            stmt.executeUpdate();
        });
    }

    @Test
    void insertAddition_bothFieldsNull_allowed() {
        assertDoesNotThrow(() -> {
            PreparedStatement stmt = connection.prepareStatement(
                "INSERT INTO assignment_additions (assignment_id, estimated_time, summary) VALUES (?, ?, ?)"
            );
            stmt.setInt(1, assignmentId);
            stmt.setNull(2, Types.INTEGER);
            stmt.setNull(3, Types.CLOB);
            stmt.executeUpdate();
        });
    }

    @Test
    void insertAddition_invalidAssignmentId_rejected() {
        assertThrows(SQLException.class, () -> {
            PreparedStatement stmt = connection.prepareStatement(
                "INSERT INTO assignment_additions (assignment_id, estimated_time, summary) VALUES (?, ?, ?)"
            );
            stmt.setInt(1, 99999);
            stmt.setInt(2, 60);
            stmt.setString(3, "Ghost summary.");
            stmt.executeUpdate();
        });
    }
}