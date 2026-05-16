package com.oiloncanvas.backend.dto.canvasapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests that our Canvas data classes correctly parse JSON
 * that looks like what the real Canvas API sends back.
 * 
 * run by <./gradlew test> in apps/backend
 */

class CanvasDtoTest {
    //json parser
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Check that a Canvas course JSON gets parsed into a CanvasCourse
     * object correctly — including fields like "course_code" that get
     * mapped to camelCase (courseCode) by the @JsonProperty annotation.
     */
    @Test
    void deserializeCanvasCourse() throws Exception {
        String json = """
            {
              "id": 12345,
              "name": "Linear Algebra",
              "course_code": "MATH340",
              "enrollment_term_id": 99,
              "start_at": "2026-01-20T00:00:00Z",
              "end_at": "2026-05-15T00:00:00Z",
              "time_zone": "America/Chicago",
              "workflow_state": "available",
              "default_view": "modules"
            }
            """;
        // make course object from json
        CanvasCourse course = mapper.readValue(json, CanvasCourse.class);

        //check instantiation of all fields.
        assertEquals(12345, course.getId());
        assertEquals("Linear Algebra", course.getName());
        assertEquals("MATH340", course.getCourseCode());
        assertEquals(99L, course.getEnrollmentTermId());
        assertEquals("2026-01-20T00:00:00Z", course.getStartAt());
        assertEquals("2026-05-15T00:00:00Z", course.getEndAt());
        assertEquals("America/Chicago", course.getTimeZone());
    }

    /**
     * Check that a Canvas assignment JSON gets parsed correctly,
     * including null values, list fields, and booleans.
     */
    @Test
    void deserializeCanvasAssignment() throws Exception {
        String json = """
            {
              "id": 67890,
              "name": "Homework 7 - Eigenvalues",
              "description": "<p>Complete problems on eigenvalues and eigenvectors.</p>",
              "due_at": "2026-03-14T23:59:00Z",
              "lock_at": null,
              "unlock_at": "2026-03-01T00:00:00Z",
              "course_id": 12345,
              "points_possible": 50.0,
              "submission_types": ["online_upload"],
              "has_submitted_submissions": false,
              "html_url": "https://canvas.wisc.edu/courses/12345/assignments/67890",
              "grading_type": "points",
              "position": 3
            }
            """;
        // make assignment object from json
        CanvasAssignment assignment = mapper.readValue(json, CanvasAssignment.class);
        
        //check instantiation of all fields.
        assertEquals(67890, assignment.getId());
        assertEquals("Homework 7 - Eigenvalues", assignment.getName());
        assertEquals("<p>Complete problems on eigenvalues and eigenvectors.</p>", assignment.getDescription());
        assertEquals("2026-03-14T23:59:00Z", assignment.getDueAt());
        assertNull(assignment.getLockAt());
        assertEquals("2026-03-01T00:00:00Z", assignment.getUnlockAt());
        assertEquals(12345, assignment.getCourseId());
        assertEquals(50.0, assignment.getPointsPossible());
        assertEquals(List.of("online_upload"), assignment.getSubmissionTypes());
        assertFalse(assignment.getHasSubmittedSubmissions());
        assertEquals("https://canvas.wisc.edu/courses/12345/assignments/67890", assignment.getHtmlUrl());
    }

    /**
     * Check that a Canvas user profile JSON gets parsed into a
     * CanvasUser object correctly.
     */
    @Test
    void deserializeCanvasUser() throws Exception {
        String json = """
            {
              "id": 11111,
              "name": "Dylan Zinsley",
              "short_name": "Dylan",
              "login_id": "dzinsley@wisc.edu",
              "email": "dzinsley@wisc.edu",
              "avatar_url": "https://canvas.wisc.edu/images/thumbnails/11111/abc",
              "locale": "en",
              "created_at": "2024-09-01T12:00:00Z"
            }
            """;
        // make user object from json
        CanvasUser user = mapper.readValue(json, CanvasUser.class);

        //check instantiation of all fields.
        assertEquals(11111, user.getId());
        assertEquals("Dylan Zinsley", user.getName());
        assertEquals("Dylan", user.getShortName());
        assertEquals("dzinsley@wisc.edu", user.getLoginId());
        assertEquals("dzinsley@wisc.edu", user.getEmail());
        assertTrue(user.getAvatarUrl().contains("11111"));
    }

    /**
     * Check that extra JSON fields we don't have in our classes
     * are just ignored (no errors) because of @JsonIgnoreProperties.
     */
    @Test
    void unknownFieldsAreIgnored() throws Exception {
        // ignore fields we are not using
        String json = """
            {
              "id": 1,
              "name": "Test Course",
              "course_code": "TEST101",
              "uuid": "abc-def-123",
              "workflow_state": "available",
              "account_id": 42,
              "grading_standard_id": null,
              "is_public": false
            }
            """;
        // make course object from json
        CanvasCourse course = mapper.readValue(json, CanvasCourse.class);
        //check instantiation of fields we want, such that
        // no error is thrown for unknown fields and they are ignored.
        assertEquals(1, course.getId());
        assertEquals("Test Course", course.getName());
        assertEquals("TEST101", course.getCourseCode());
    }
}
