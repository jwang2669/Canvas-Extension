package com.oiloncanvas.backend.database;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;

public class TodoItemTest extends DatabaseTest {

    private int oocId;

    @BeforeEach
    void setup() throws SQLException {
        oocId = insertUser();
    }

    @Test
    void insertTodo_requiredFieldsOnly() throws SQLException {
        PreparedStatement stmt = connection.prepareStatement(
            "INSERT INTO todo_item (ooc_id, description) VALUES (?, ?)",
            Statement.RETURN_GENERATED_KEYS
        );
        stmt.setInt(1, oocId);
        stmt.setString(2, "Read chapter 5");
        stmt.executeUpdate();
        int todoId = getGeneratedKey(stmt);

        PreparedStatement query = connection.prepareStatement(
            "SELECT * FROM todo_item WHERE todo_id = ?"
        );
        query.setInt(1, todoId);
        ResultSet rs = query.executeQuery();

        assertTrue(rs.next());
        assertEquals(oocId,           rs.getInt("ooc_id"));
        assertEquals("Read chapter 5", rs.getString("description"));
    }

    @Test
    void insertTodo_defaultCompleted_isFalse() throws SQLException {
        PreparedStatement stmt = connection.prepareStatement(
            "INSERT INTO todo_item (ooc_id, description) VALUES (?, ?)",
            Statement.RETURN_GENERATED_KEYS
        );
        stmt.setInt(1, oocId);
        stmt.setString(2, "Submit HW");
        stmt.executeUpdate();
        int todoId = getGeneratedKey(stmt);

        PreparedStatement query = connection.prepareStatement(
            "SELECT completed FROM todo_item WHERE todo_id = ?"
        );
        query.setInt(1, todoId);
        ResultSet rs = query.executeQuery();
        rs.next();

        assertFalse(rs.getBoolean("completed"));
    }

    @Test
    void updateTodo_markCompleted() throws SQLException {
        PreparedStatement insert = connection.prepareStatement(
            "INSERT INTO todo_item (ooc_id, description) VALUES (?, ?)",
            Statement.RETURN_GENERATED_KEYS
        );
        insert.setInt(1, oocId);
        insert.setString(2, "Submit HW");
        insert.executeUpdate();
        int todoId = getGeneratedKey(insert);

        PreparedStatement update = connection.prepareStatement(
            "UPDATE todo_item SET completed = TRUE WHERE todo_id = ?"
        );
        update.setInt(1, todoId);
        update.executeUpdate();

        PreparedStatement query = connection.prepareStatement(
            "SELECT completed FROM todo_item WHERE todo_id = ?"
        );
        query.setInt(1, todoId);
        ResultSet rs = query.executeQuery();
        rs.next();

        assertTrue(rs.getBoolean("completed"));
    }

    @Test
    void insertTodo_allOptionalFields() throws SQLException {
        PreparedStatement stmt = connection.prepareStatement(
            "INSERT INTO todo_item (ooc_id, description, task_type, estimated_minutes, course_code, due_date) VALUES (?, ?, ?, ?, ?, ?)",
            Statement.RETURN_GENERATED_KEYS
        );
        stmt.setInt(1, oocId);
        stmt.setString(2, "Submit lab report");
        stmt.setString(3, "assignment");       // task_type
        stmt.setInt(4, 90);                    // estimated_minutes
        stmt.setString(5, "BIO101");           // course_code
        stmt.setTimestamp(6, Timestamp.valueOf("2025-05-01 23:59:00")); // due_date
        stmt.executeUpdate();
        int todoId = getGeneratedKey(stmt);

        PreparedStatement query = connection.prepareStatement(
            "SELECT * FROM todo_item WHERE todo_id = ?"
        );
        query.setInt(1, todoId);
        ResultSet rs = query.executeQuery();

        assertTrue(rs.next());
        assertEquals("assignment", rs.getString("task_type"));
        assertEquals(90,           rs.getInt("estimated_minutes"));
        assertEquals("BIO101",     rs.getString("course_code"));
        assertNotNull(             rs.getTimestamp("due_date"));
    }

    @Test
    void insertTodo_nullDescription_rejected() {
        assertThrows(SQLException.class, () -> {
            PreparedStatement stmt = connection.prepareStatement(
                "INSERT INTO todo_item (ooc_id, description) VALUES (?, ?)"
            );
            stmt.setInt(1, oocId);
            stmt.setNull(2, Types.VARCHAR);
            stmt.executeUpdate();
        });
    }

    @Test
    void insertTodo_invalidOocId_rejected() {
        assertThrows(SQLException.class, () -> {
            PreparedStatement stmt = connection.prepareStatement(
                "INSERT INTO todo_item (ooc_id, description) VALUES (?, ?)"
            );
            stmt.setInt(1, 99999);
            stmt.setString(2, "Ghost task");
            stmt.executeUpdate();
        });
    }

    @Test
    void todos_isolatedByUser() throws SQLException {
        int otherOocId = insertUser();

        PreparedStatement insert = connection.prepareStatement(
            "INSERT INTO todo_item (ooc_id, description) VALUES (?, ?)"
        );
        insert.setInt(1, oocId);
        insert.setString(2, "My task");
        insert.executeUpdate();

        insert.setInt(1, otherOocId);
        insert.setString(2, "Their task");
        insert.executeUpdate();

        PreparedStatement query = connection.prepareStatement(
            "SELECT COUNT(*) FROM todo_item WHERE ooc_id = ?"
        );
        query.setInt(1, oocId);
        ResultSet rs = query.executeQuery();
        rs.next();

        assertEquals(1, rs.getInt(1));
    }
}