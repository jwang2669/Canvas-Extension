package com.oiloncanvas.backend.database;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;

public class CanvasConnectionTest extends DatabaseTest {

    private int oocId;
    private int canvasId;

    @BeforeEach
    void setup() throws SQLException {
        oocId     = insertUser();
        canvasId  = insertCanvasInstance("https://canvas.wisc.edu");
    }

    @Test
    void insertConnection_storesAccessToken() throws SQLException {
        PreparedStatement stmt = connection.prepareStatement(
            "INSERT INTO canvas_connection (ooc_id, canvas_id, access_token) VALUES (?, ?, ?)"
        );
        stmt.setInt(1, oocId);
        stmt.setInt(2, canvasId);
        stmt.setString(3, "token-abc");
        stmt.executeUpdate();

        PreparedStatement query = connection.prepareStatement(
            "SELECT access_token FROM canvas_connection WHERE ooc_id = ? AND canvas_id = ?"
        );
        query.setInt(1, oocId);
        query.setInt(2, canvasId);
        ResultSet rs = query.executeQuery();

        assertTrue(rs.next());
        assertEquals("token-abc", rs.getString("access_token"));
    }

    @Test
    void insertConnection_invalidOocId_rejected() {
        assertThrows(SQLException.class, () -> {
            PreparedStatement stmt = connection.prepareStatement(
                "INSERT INTO canvas_connection (ooc_id, canvas_id, access_token) VALUES (?, ?, ?)"
            );
            stmt.setInt(1, 99999);
            stmt.setInt(2, canvasId);
            stmt.setString(3, "token-abc");
            stmt.executeUpdate();
        });
    }

    @Test
    void insertConnection_invalidCanvasId_rejected() {
        assertThrows(SQLException.class, () -> {
            PreparedStatement stmt = connection.prepareStatement(
                "INSERT INTO canvas_connection (ooc_id, canvas_id, access_token) VALUES (?, ?, ?)"
            );
            stmt.setInt(1, oocId);
            stmt.setInt(2, 99999);
            stmt.setString(3, "token-abc");
            stmt.executeUpdate();
        });
    }

    @Test
    void insertConnection_nullAccessToken_rejected() {
        assertThrows(SQLException.class, () -> {
            PreparedStatement stmt = connection.prepareStatement(
                "INSERT INTO canvas_connection (ooc_id, canvas_id, access_token) VALUES (?, ?, ?)"
            );
            stmt.setInt(1, oocId);
            stmt.setInt(2, canvasId);
            stmt.setNull(3, Types.VARCHAR);
            stmt.executeUpdate();
        });
    }
}