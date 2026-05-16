package com.oiloncanvas.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.oiloncanvas.backend.client.CanvasApiClient;
import com.oiloncanvas.backend.dto.AssignmentsResponse;
import com.oiloncanvas.backend.dto.EnrolledCoursesResponse;
import com.oiloncanvas.backend.dto.SessionResponse;
import com.oiloncanvas.backend.dto.canvasapi.CanvasCourse;
import com.oiloncanvas.backend.dto.canvasapi.CanvasUser;
import com.oiloncanvas.backend.entity.AssignmentCache;
import com.oiloncanvas.backend.entity.CourseCache;
import com.oiloncanvas.backend.repository.AssignmentCacheRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class CanvasCourseServiceTest {

    @Mock
    private CanvasApiClient canvasApiClient;

    @Mock
    private SessionService sessionService;

    @Mock
    private AssignmentCacheRepository assignmentCacheRepository;

        /**
         * Verifies profile lookup fails with 503 when Canvas client is unavailable.
         */
    @Test
    void getCurrentUserProfile_whenCanvasNotConfigured_throws503() {
        CanvasCourseService service =
                new CanvasCourseService(Optional.empty(), sessionService, assignmentCacheRepository);

        ResponseStatusException ex =
                assertThrows(ResponseStatusException.class, service::getCurrentUserProfile);

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, ex.getStatusCode());
    }

        /**
         * Verifies profile lookup fails with 502 when Canvas returns no user payload.
         */
        @Test
        void getCurrentUserProfile_whenClientReturnsNullUser_throws502() {
        CanvasCourseService service =
                new CanvasCourseService(
                        Optional.of(canvasApiClient), sessionService, assignmentCacheRepository);

        when(canvasApiClient.getCurrentUser()).thenReturn(null);

        ResponseStatusException ex =
                assertThrows(ResponseStatusException.class, service::getCurrentUserProfile);

        assertEquals(HttpStatus.BAD_GATEWAY, ex.getStatusCode());
    }

        /**
         * Verifies enrolled-course response keeps only valid available courses.
         */
    @Test
    void getCurrentlyEnrolledCourses_filtersOutInvalidCourses() {
        CanvasCourseService service =
                new CanvasCourseService(
                        Optional.of(canvasApiClient), sessionService, assignmentCacheRepository);

        CanvasCourse blankName = new CanvasCourse();
        blankName.setId(1);
        blankName.setName("   ");
        blankName.setWorkflowState("available");

        CanvasCourse unavailable = new CanvasCourse();
        unavailable.setId(2);
        unavailable.setName("Archived Course");
        unavailable.setWorkflowState("completed");

        CanvasCourse valid = new CanvasCourse();
        valid.setId(3);
        valid.setName("Software Engineering");
        valid.setWorkflowState("available");

        when(canvasApiClient.getCoursesWithActiveEnrollment())
                .thenReturn(List.of(blankName, unavailable, valid));
        when(canvasApiClient.getAssignmentsForCourse(any(Integer.class))).thenReturn(List.of());
        CanvasUser user = new CanvasUser();
        user.setLoginId("student123");
        when(canvasApiClient.getCurrentUser()).thenReturn(user);

        SessionResponse session =
                new SessionResponse("session-1", "student123", List.of("CS506"), "2026-04-22T10:00:00");
        when(sessionService.fromCanvasData(any(CanvasUser.class), any(List.class), any()))
                .thenReturn(session);

        EnrolledCoursesResponse response = service.getCurrentlyEnrolledCourses();

        assertEquals(1, response.getCourses().size());
        assertEquals("Software Engineering", response.getCourses().get(0).getName());
    }

        /**
         * Verifies blank Canvas login id falls back to "canvas-self" for session creation.
         */
    @Test
    void getCurrentlyEnrolledCourses_whenLoginIdMissing_usesFallbackAndSetsSessionFields() {
        CanvasCourseService service =
                new CanvasCourseService(
                        Optional.of(canvasApiClient), sessionService, assignmentCacheRepository);

        CanvasCourse valid = new CanvasCourse();
        valid.setId(7);
        valid.setName("CS 506");
        valid.setWorkflowState("available");

        when(canvasApiClient.getCoursesWithActiveEnrollment()).thenReturn(List.of(valid));
        when(canvasApiClient.getAssignmentsForCourse(eq(7))).thenReturn(List.of());

        CanvasUser userWithoutLogin = new CanvasUser();
        userWithoutLogin.setLoginId("   ");
        when(canvasApiClient.getCurrentUser()).thenReturn(userWithoutLogin);

        SessionResponse session =
                new SessionResponse(
                        "session-fallback", "canvas-self", List.of("CS506"), "2026-04-22T10:00:00");
        when(sessionService.fromCanvasData(any(CanvasUser.class), any(List.class), any()))
                .thenReturn(session);

        EnrolledCoursesResponse response = service.getCurrentlyEnrolledCourses();

        assertEquals("session-fallback", response.getSessionId());
        assertEquals("canvas-self", response.getUserId());
        assertNotNull(response.getCourses());
        assertEquals(1, response.getCourses().size());

        ArgumentCaptor<CanvasUser> userCaptor = ArgumentCaptor.forClass(CanvasUser.class);
        verify(sessionService).fromCanvasData(userCaptor.capture(), any(List.class), any());
        assertEquals("canvas-self", userCaptor.getValue().getLoginId());
    }

    /**
     * Verifies assignment mapping defaults estimated minutes and unknown course name when missing.
     */
    @Test
    void getAssignmentsForWeek_mapsDefaultEstimatedMinutesAndUnknownCourse() {
        CanvasCourseService service =
                new CanvasCourseService(
                        Optional.of(canvasApiClient), sessionService, assignmentCacheRepository);

        AssignmentCache withNoCourse = new AssignmentCache();
        withNoCourse.setCanvasAssignmentId(1001);
        withNoCourse.setAssignmentName("Reading Reflection");
        withNoCourse.setDueDate(LocalDateTime.of(2026, 4, 22, 23, 59));
        withNoCourse.setCourseCache(null);

        CourseCache course = new CourseCache();
        course.setCourseName("Software Engineering");
        AssignmentCache withCourse = new AssignmentCache();
        withCourse.setCanvasAssignmentId(1002);
        withCourse.setAssignmentName("Sprint Report");
        withCourse.setDueDate(LocalDateTime.of(2026, 4, 23, 23, 59));
        withCourse.setCourseCache(course);

        when(assignmentCacheRepository.findByDueDateBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(withNoCourse, withCourse));

        AssignmentsResponse response = service.getAssignmentsForWeek(null);

        assertEquals(2, response.getAssignments().size());

        assertEquals(60, response.getAssignments().get(0).getEstimatedMinutes());
        assertEquals("Unknown Course", response.getAssignments().get(0).getCourseName());
        assertEquals("Software Engineering", response.getAssignments().get(1).getCourseName());
    }
}
