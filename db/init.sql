CREATE DATABASE IF NOT EXISTS ooc;
USE ooc;

-- Create tables

-- stores OOC user ids
DROP TABLE IF EXISTS ooc_user;
CREATE TABLE ooc_user (
    ooc_id INT AUTO_INCREMENT PRIMARY KEY,
    external_user_id VARCHAR(255),
    current_session_id VARCHAR(64),
    active_course_ids TEXT,
    session_created_at TIMESTAMP NULL
);

-- stores url for canvas instance, e.g. https://canvas.wisc.com
DROP TABLE IF EXISTS canvas_instance;
CREATE TABLE canvas_instance (
    canvas_id INT AUTO_INCREMENT PRIMARY KEY,
    base_url VARCHAR(255) NOT NULL
);

-- maps ooc user to canvas instance and stores access token for API calls
DROP TABLE IF EXISTS canvas_connection;
CREATE TABLE canvas_connection (
    connection_id INT AUTO_INCREMENT PRIMARY KEY,
    ooc_id INT NOT NULL,
    canvas_id INT NOT NULL,
    access_token VARCHAR(255) NOT NULL,
    FOREIGN KEY (ooc_id) REFERENCES ooc_user(ooc_id),
    FOREIGN KEY (canvas_id) REFERENCES canvas_instance(canvas_id)
);

-- keeps course info for courses the user is enrolled in, so we don't have to make API calls every time
DROP TABLE IF EXISTS course_cache;
CREATE TABLE course_cache (
    course_id INT AUTO_INCREMENT PRIMARY KEY,
    ooc_id INT NOT NULL,
    canvas_course_id INT NOT NULL,
    course_name VARCHAR(255) NOT NULL,
    course_code VARCHAR(255) NOT NULL,
    FOREIGN KEY (ooc_id) REFERENCES ooc_user(ooc_id)
);

-- keeps assignment info for assignments in courses the user is enrolled in, so we don't have to make API calls every time
DROP TABLE IF EXISTS assignment_cache;
CREATE TABLE assignment_cache (
    assignment_id INT AUTO_INCREMENT PRIMARY KEY,
    canvas_assignment_id INT NOT NULL,
    course_id INT NOT NULL,
    assignment_name VARCHAR(255) NOT NULL,
    due_date DATETIME,
    FOREIGN KEY (course_id) REFERENCES course_cache(course_id)
);

-- keeps additional info for assignments, i.e. estimated time to complete, summary, etc.
DROP TABLE IF EXISTS assignment_additions;
CREATE TABLE assignment_additions (
    addition_id INT AUTO_INCREMENT PRIMARY KEY,
    assignment_id INT NOT NULL,
    estimated_time INT,
    summary TEXT,
    FOREIGN KEY (assignment_id) REFERENCES assignment_cache(assignment_id)
);

-- stores todo items for the user
DROP TABLE IF EXISTS todo_item;
CREATE TABLE todo_item (
    todo_id INT AUTO_INCREMENT PRIMARY KEY,
    ooc_id INT NOT NULL,
    description TEXT NOT NULL,
    task_type VARCHAR(100),
    estimated_minutes INT,
    course_code VARCHAR(255),
    due_date DATETIME,
    completed BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (ooc_id) REFERENCES ooc_user(ooc_id)
);