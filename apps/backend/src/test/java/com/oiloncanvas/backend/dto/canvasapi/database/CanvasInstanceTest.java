package com.oiloncanvas.backend.database;

import org.junit.jupiter.api.Test;

import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;

public class CanvasInstanceTest extends DatabaseTest {

    @Test
    void insertInstance_storesBaseUrl() throws SQLException {
        int canvasId = insertCanvasInstance("https://canvas.wisc.edu");

        PreparedStatement query = connection.prepareStatement(
            "SELECT base_url FROM canvas_instance WHERE canvas_id = ?"
        );
        query.setInt(1, canvasId);
        ResultSet rs = query.executeQuery();

        assertTrue(rs.next());
        assertEquals("https://canvas.wisc.edu", rs.getString("base_url"));
    }

    @Test
    void insertInstance_nullBaseUrl_rejected() {
        assertThrows(SQLException.class, () -> {
            PreparedStatement stmt = connection.prepareStatement(
                "INSERT INTO canvas_instance (base_url) VALUES (?)"
            );
            stmt.setNull(1, Types.VARCHAR);
            stmt.executeUpdate();
        });
    }
}