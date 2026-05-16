package com.oiloncanvas.backend.database;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import java.sql.*;

public abstract class DatabaseTest {

    protected static Connection connection;

    @BeforeAll
    static void connect() throws SQLException {
        String url = "jdbc:h2:mem:testdb;MODE=MySQL;DB_CLOSE_DELAY=-1";
        String user = "sa";
        String password = "";

        connection = DriverManager.getConnection(url, user, password);

        initSchema();
    }

    private static void initSchema() throws SQLException {
        Statement stmt = connection.createStatement();

        stmt.execute("SET REFERENTIAL_INTEGRITY FALSE");

        stmt.execute("DROP TABLE IF EXISTS todo_item");
        stmt.execute("DROP TABLE IF EXISTS assignment_additions");
        stmt.execute("DROP TABLE IF EXISTS assignment_cache");
        stmt.execute("DROP TABLE IF EXISTS course_cache");
        stmt.execute("DROP TABLE IF EXISTS canvas_connection");
        stmt.execute("DROP TABLE IF EXISTS canvas_instance");
        stmt.execute("DROP TABLE IF EXISTS ooc_user");

        stmt.execute("SET REFERENTIAL_INTEGRITY TRUE");
        stmt.execute("""
            CREATE TABLE ooc_user (
                ooc_id INT AUTO_INCREMENT PRIMARY KEY,
                external_user_id VARCHAR(255),
                current_session_id VARCHAR(64),
                active_course_ids VARCHAR(2000),
                session_created_at TIMESTAMP NULL
            );
        """);

        stmt.execute("""
            CREATE TABLE canvas_instance (
                canvas_id INT AUTO_INCREMENT PRIMARY KEY,
                base_url VARCHAR(255) NOT NULL
            );
        """);

        stmt.execute("""
            CREATE TABLE canvas_connection (
                connection_id INT AUTO_INCREMENT PRIMARY KEY,
                ooc_id INT NOT NULL,
                canvas_id INT NOT NULL,
                access_token VARCHAR(255) NOT NULL,
                FOREIGN KEY (ooc_id) REFERENCES ooc_user(ooc_id),
                FOREIGN KEY (canvas_id) REFERENCES canvas_instance(canvas_id)
            );
        """);

        stmt.execute("""
            CREATE TABLE course_cache (
                course_id INT AUTO_INCREMENT PRIMARY KEY,
                ooc_id INT NOT NULL,
                canvas_course_id INT NOT NULL,
                course_name VARCHAR(255) NOT NULL,
                course_code VARCHAR(255) NOT NULL,
                FOREIGN KEY (ooc_id) REFERENCES ooc_user(ooc_id)
            );
        """);

        stmt.execute("""
            CREATE TABLE assignment_cache (
                assignment_id INT AUTO_INCREMENT PRIMARY KEY,
                canvas_assignment_id INT NOT NULL,
                course_id INT NOT NULL,
                assignment_name VARCHAR(255) NOT NULL,
                due_date TIMESTAMP,
                FOREIGN KEY (course_id) REFERENCES course_cache(course_id)
            );
        """);

        stmt.execute("""
            CREATE TABLE assignment_additions (
                addition_id INT AUTO_INCREMENT PRIMARY KEY,
                assignment_id INT NOT NULL,
                estimated_time INT,
                summary VARCHAR(2000),
                FOREIGN KEY (assignment_id) REFERENCES assignment_cache(assignment_id)
            );
        """);

        stmt.execute("""
            CREATE TABLE todo_item (
                todo_id INT AUTO_INCREMENT PRIMARY KEY,
                ooc_id INT NOT NULL,
                description VARCHAR(2000) NOT NULL,
                task_type VARCHAR(100),
                estimated_minutes INT,
                course_code VARCHAR(255),
                due_date TIMESTAMP,
                completed BOOLEAN DEFAULT FALSE,
                FOREIGN KEY (ooc_id) REFERENCES ooc_user(ooc_id)
            );
        """);
    }

    @AfterAll
    static void disconnect() throws SQLException {
        if (connection != null && !connection.isClosed())
            connection.close();
    }

    @BeforeEach
    void beginTransaction() throws SQLException {
        connection.setAutoCommit(false);
    }

    @AfterEach
    void rollback() throws SQLException {
        connection.rollback();
    }


    protected int insertUser() throws SQLException {
        PreparedStatement stmt = connection.prepareStatement(
            "INSERT INTO ooc_user (external_user_id, current_session_id, active_course_ids, session_created_at) VALUES (?, ?, ?, ?)",
            Statement.RETURN_GENERATED_KEYS
        );

        stmt.setNull(1, Types.VARCHAR);
        stmt.setNull(2, Types.VARCHAR);
        stmt.setNull(3, Types.VARCHAR);
        stmt.setNull(4, Types.TIMESTAMP);

stmt.executeUpdate();
        return getGeneratedKey(stmt);
    }

    protected int insertCanvasInstance(String baseUrl) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement(
            "INSERT INTO canvas_instance (base_url) VALUES (?)",
            Statement.RETURN_GENERATED_KEYS
        );
        stmt.setString(1, baseUrl);
        stmt.executeUpdate();
        return getGeneratedKey(stmt);
    }

    protected int insertCourse(int oocId, int canvasCourseId, String name, String code) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement(
            "INSERT INTO course_cache (ooc_id, canvas_course_id, course_name, course_code) VALUES (?, ?, ?, ?)",
            Statement.RETURN_GENERATED_KEYS
        );
        stmt.setInt(1, oocId);
        stmt.setInt(2, canvasCourseId);
        stmt.setString(3, name);
        stmt.setString(4, code);
        stmt.executeUpdate();
        return getGeneratedKey(stmt);
    }

    protected int insertAssignment(int courseId, int canvasAssignmentId, String name, Timestamp dueDate) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement(
            "INSERT INTO assignment_cache (canvas_assignment_id, course_id, assignment_name, due_date) VALUES (?, ?, ?, ?)",
            Statement.RETURN_GENERATED_KEYS
        );
        stmt.setInt(1, canvasAssignmentId);
        stmt.setInt(2, courseId);
        stmt.setString(3, name);
        stmt.setTimestamp(4, dueDate);
        stmt.executeUpdate();
        return getGeneratedKey(stmt);
    }

    protected int getGeneratedKey(PreparedStatement stmt) throws SQLException {
        ResultSet keys = stmt.getGeneratedKeys();
        keys.next();
        return keys.getInt(1);
    }
}