package com.oiloncanvas.backend.service;

import com.oiloncanvas.backend.dto.SessionRequest;
import com.oiloncanvas.backend.dto.SessionResponse;
import com.oiloncanvas.backend.dto.canvasapi.CanvasAssignment;
import com.oiloncanvas.backend.dto.canvasapi.CanvasCourse;
import com.oiloncanvas.backend.dto.canvasapi.CanvasUser;
import com.oiloncanvas.backend.entity.OocUser;
import com.oiloncanvas.backend.repository.OocUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for SessionService.
 * Uses Mockito to mock repositories and services.
 * 
 * Run with: ./gradlew test
 */
@ExtendWith(MockitoExtension.class)
class SessionServiceTest {

    @Mock
    private OocUserRepository oocUserRepository;

    @Mock
    private CourseCacheSyncService courseCacheSyncService;

    private SessionService sessionService;

    @BeforeEach
    void setUp() {
        sessionService = new SessionService(oocUserRepository, courseCacheSyncService);
    }

    /**
     * Test that startSession throws exception when request is null.
     */
    @Test
    void startSession_nullRequest_throwsException() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> sessionService.startSession(null)
        );

        assertEquals("Session request cannot be null.", exception.getMessage());
    }

    /**
     * Test that startSession throws exception when userId is blank.
     */
    @Test
    void startSession_blankUserId_throwsException() {
        SessionRequest request = new SessionRequest();
        request.setUserId("   ");
        request.setCourseIds(List.of("CS506"));

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> sessionService.startSession(request)
        );

        assertEquals("Session userId cannot be null or blank.", exception.getMessage());
    }

    /**
     * Test that startSession throws exception when courseIds is empty.
     */
    @Test
    void startSession_emptyCourseIds_throwsException() {
        SessionRequest request = new SessionRequest();
        request.setUserId("student123");
        request.setCourseIds(List.of());

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> sessionService.startSession(request)
        );

        assertEquals("Session courseIds cannot be null or empty.", exception.getMessage());
    }

    /**
     * Test that startSession with valid request saves user and returns session.
     */
    @Test
    void startSession_validRequest_savesUserAndReturnsSession() {
        SessionRequest request = new SessionRequest();
        request.setUserId("student123");
        request.setCourseIds(List.of("CS506", "MATH340"));

        when(oocUserRepository.findByExternalUserId("student123"))
            .thenReturn(Optional.empty());

        when(oocUserRepository.save(any(OocUser.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        SessionResponse response = sessionService.startSession(request);

        assertNotNull(response.getSessionId());
        assertEquals("student123", response.getUserId());
        assertEquals(List.of("CS506", "MATH340"), response.getCourseIds());
        assertNotNull(response.getCreatedAt());

        verify(oocUserRepository, times(1)).save(any(OocUser.class));
    }

    /**
     * Test that startSession updates existing user instead of creating new one.
     */
    @Test
    void startSession_existingUser_updatesInsteadOfCreating() {
        SessionRequest request = new SessionRequest();
        request.setUserId("existing_student");
        request.setCourseIds(List.of("CS506"));

        OocUser existingUser = new OocUser();
        existingUser.setExternalUserId("existing_student");
        existingUser.setActiveCourseIds("OLD_COURSE");

        when(oocUserRepository.findByExternalUserId("existing_student"))
            .thenReturn(Optional.of(existingUser));

        when(oocUserRepository.save(any(OocUser.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        SessionResponse response = sessionService.startSession(request);

        assertEquals("existing_student", response.getUserId());
        assertEquals(List.of("CS506"), response.getCourseIds());

        verify(oocUserRepository, times(1)).save(any(OocUser.class));
        verify(oocUserRepository, times(1)).findByExternalUserId("existing_student");
    }

    /**
     * Test that fromCanvasData extracts userId from loginId and courseIds from course codes.
     */
    @Test
    void fromCanvasData_extractsUserIdAndCourseIds() {
        CanvasUser user = new CanvasUser();
        user.setId(12345);
        user.setLoginId("canvas_student");
        user.setName("Test Student");

        CanvasCourse course1 = new CanvasCourse();
        course1.setId(100);
        course1.setCourseCode("CS506");
        course1.setName("Software Engineering");

        CanvasCourse course2 = new CanvasCourse();
        course2.setId(200);
        course2.setCourseCode("MATH340");
        course2.setName("Linear Algebra");

        List<CanvasCourse> courses = List.of(course1, course2);
        Map<Long, List<CanvasAssignment>> assignments = Collections.emptyMap();

        when(oocUserRepository.findByExternalUserId("canvas_student"))
            .thenReturn(Optional.empty());

        when(oocUserRepository.save(any(OocUser.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        SessionResponse response = sessionService.fromCanvasData(user, courses, assignments);

        assertEquals("canvas_student", response.getUserId());
        assertEquals(List.of("CS506", "MATH340"), response.getCourseIds());
        assertNotNull(response.getSessionId());

        verify(oocUserRepository, times(1)).save(any(OocUser.class));
        verify(courseCacheSyncService, times(1)).replaceForUser(any(OocUser.class), eq(courses), eq(assignments));
    }

}
