package com.oiloncanvas.backend.database;

import org.junit.jupiter.api.Test;

import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;

public class OocUserTest extends DatabaseTest {

    @Test
    void insertUser_rowExists() throws SQLException {
        int oocId = insertUser();

        PreparedStatement query = connection.prepareStatement(
            "SELECT ooc_id FROM ooc_user WHERE ooc_id = ?"
        );
        query.setInt(1, oocId);
        ResultSet rs = query.executeQuery();

        assertTrue(rs.next());
        assertEquals(oocId, rs.getInt("ooc_id"));
    }

    @Test
    void insertMultipleUsers_oocIdsAreUnique() throws SQLException {
        int id1 = insertUser();
        int id2 = insertUser();

        assertNotEquals(id1, id2);
    }
}