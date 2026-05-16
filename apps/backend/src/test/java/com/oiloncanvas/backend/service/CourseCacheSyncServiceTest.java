package com.oiloncanvas.backend.service;
 
import com.oiloncanvas.backend.dto.canvasapi.CanvasAssignment;
import com.oiloncanvas.backend.dto.canvasapi.CanvasCourse;
import com.oiloncanvas.backend.entity.AssignmentCache;
import com.oiloncanvas.backend.entity.CourseCache;
import com.oiloncanvas.backend.entity.OocUser;
import com.oiloncanvas.backend.repository.AssignmentAdditionsRepository;
import com.oiloncanvas.backend.repository.AssignmentCacheRepository;
import com.oiloncanvas.backend.repository.CourseCacheRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
 
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
 
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
 
/**
 * Unit tests for CourseCacheSyncService cache validation.
 * Verifies that replaceForUser correctly deletes stale data and inserts fresh data.
 */
@ExtendWith(MockitoExtension.class)
class CourseCacheSyncServiceTest {
 
    @Mock
    private CourseCacheRepository courseCacheRepository;
 
    @Mock
    private AssignmentCacheRepository assignmentCacheRepository;
 
    @Mock
    private AssignmentAdditionsRepository assignmentAdditionsRepository;
 
    private CourseCacheSyncService syncService;
 
    private OocUser testUser;
 
    @BeforeEach
    void setUp() {
        syncService = new CourseCacheSyncService(
            courseCacheRepository,
            assignmentCacheRepository,
            assignmentAdditionsRepository
        );
 
        testUser = new OocUser();
        testUser.setExternalUserId("student123");
        // set oocId via reflection since there's no setter
        try {
            var field = OocUser.class.getDeclaredField("oocId");
            field.setAccessible(true);
            field.set(testUser, 1);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set oocId", e);
        }
    }
 
    /**
     * replaceForUser should do nothing when user is null
     */
    @Test
    void nullUserDoesNothing() {
        syncService.replaceForUser(null, List.of(), Collections.emptyMap());
 
        verifyNoInteractions(courseCacheRepository);
        verifyNoInteractions(assignmentCacheRepository);
    }
 
    /**
     * replaceForUser should do nothing when user has no oocId (not yet persisted)
     */
    @Test
    void nullOocIdDoesNothing() {
        OocUser unpersisted = new OocUser();
        syncService.replaceForUser(unpersisted, List.of(), Collections.emptyMap());
 
        verifyNoInteractions(courseCacheRepository);
        verifyNoInteractions(assignmentCacheRepository);
    }
 
    /**
     * replaceForUser with an empty course list should still delete existing rows
     */
    @Test
    void emptyCourseListDeletesExistingRows() {
        CourseCache existingCourse = buildCourseCache(testUser, 101, "Old Course", "OLD101");
        when(courseCacheRepository.findByOocUser(testUser)).thenReturn(List.of(existingCourse));
        when(assignmentCacheRepository.findByCourseCache(existingCourse)).thenReturn(List.of());
 
        syncService.replaceForUser(testUser, List.of(), Collections.emptyMap());
 
        verify(courseCacheRepository).delete(existingCourse);
        verify(courseCacheRepository, never()).save(any());
    }

    // Delete-before-insert (stale data removal) tests

    /**
     * Old course rows for the user must be deleted before new ones are inserted
     * This ensures stale Canvas data never persists across syncs
     */
    @Test
    void deletesOldCoursesBeforeInserting() {
        CourseCache stale = buildCourseCache(testUser, 999, "Stale Course", "STALE101");
        when(courseCacheRepository.findByOocUser(testUser)).thenReturn(List.of(stale));
        when(assignmentCacheRepository.findByCourseCache(stale)).thenReturn(List.of());
        when(courseCacheRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
 
        CanvasCourse freshCourse = buildCanvasCourse(200, "Fresh Course", "FRESH101");
        syncService.replaceForUser(testUser, List.of(freshCourse), Collections.emptyMap());
 
        verify(courseCacheRepository).delete(stale);
 
        ArgumentCaptor<CourseCache> captor = ArgumentCaptor.forClass(CourseCache.class);
        verify(courseCacheRepository).save(captor.capture());
        assertEquals("Fresh Course", captor.getValue().getCourseName());
        assertEquals("FRESH101", captor.getValue().getCourseCode());
    }
 
    /**
     * Old assignment rows linked to a course must be deleted when the course is replaced.
     */
    @Test
    void deletesOldAssignmentsWithOldCourse() {
        CourseCache stale = buildCourseCache(testUser, 999, "Stale Course", "STALE101");
        AssignmentCache staleAssignment = new AssignmentCache();
        staleAssignment.setCourseCache(stale);
        staleAssignment.setCanvasAssignmentId(5001);
        staleAssignment.setAssignmentName("Old HW");
 
        when(courseCacheRepository.findByOocUser(testUser)).thenReturn(List.of(stale));
        when(assignmentCacheRepository.findByCourseCache(stale)).thenReturn(List.of(staleAssignment));
        when(assignmentAdditionsRepository.findByAssignmentCache(staleAssignment)).thenReturn(null);
 
        syncService.replaceForUser(testUser, List.of(), Collections.emptyMap());
 
        verify(assignmentCacheRepository).delete(staleAssignment);
        verify(courseCacheRepository).delete(stale);
    }

    // fresh data insertion tests
 
    /**
     * After sync, course fields must be persisted with correct values
     */
    @Test
    void savesNewCourseWithCorrectFields() {
        when(courseCacheRepository.findByOocUser(testUser)).thenReturn(List.of());
        when(courseCacheRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
 
        CanvasCourse course = buildCanvasCourse(300, "Operating Systems", "CS537");
        syncService.replaceForUser(testUser, List.of(course), Collections.emptyMap());
 
        ArgumentCaptor<CourseCache> captor = ArgumentCaptor.forClass(CourseCache.class);
        verify(courseCacheRepository).save(captor.capture());
        CourseCache saved = captor.getValue();
 
        assertEquals(testUser, saved.getOocUser());
        assertEquals(300, saved.getCanvasCourseId());
        assertEquals("Operating Systems", saved.getCourseName());
        assertEquals("CS537", saved.getCourseCode());
    }
 
    /**
     * Assignments provided in the map must be saved under the correct course
     */
    @Test
    void savesAssignmentsForCourse() {
        when(courseCacheRepository.findByOocUser(testUser)).thenReturn(List.of());
        when(courseCacheRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
 
        CanvasCourse course = buildCanvasCourse(300, "Operating Systems", "CS537");
        CanvasAssignment assignment = buildCanvasAssignment(7001, "Project 2", "2026-04-25T23:59:00Z");
        Map<Long, List<CanvasAssignment>> assignments = Map.of(300L, List.of(assignment));
 
        syncService.replaceForUser(testUser, List.of(course), assignments);
 
        ArgumentCaptor<AssignmentCache> captor = ArgumentCaptor.forClass(AssignmentCache.class);
        verify(assignmentCacheRepository).save(captor.capture());
        AssignmentCache saved = captor.getValue();
 
        assertEquals(7001, saved.getCanvasAssignmentId());
        assertEquals("Project 2", saved.getAssignmentName());
        assertNotNull(saved.getDueDate());
    }
 
    /**
     * An assignment with a null due_at from Canvas should be stored with a null due date,
     * not throw an exception
     */
    @Test
    void assignmentWithNullDueDateSaved() {
        when(courseCacheRepository.findByOocUser(testUser)).thenReturn(List.of());
        when(courseCacheRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
 
        CanvasCourse course = buildCanvasCourse(300, "Operating Systems", "CS537");
        CanvasAssignment assignment = buildCanvasAssignment(7002, "Optional Reading", null);
        Map<Long, List<CanvasAssignment>> assignments = Map.of(300L, List.of(assignment));
 
        syncService.replaceForUser(testUser, List.of(course), assignments);
 
        ArgumentCaptor<AssignmentCache> captor = ArgumentCaptor.forClass(AssignmentCache.class);
        verify(assignmentCacheRepository).save(captor.capture());
        assertNull(captor.getValue().getDueDate());
    }
 
    /**
     * Multiple courses should each get their own assignments saved separately
     */
    @Test
    void multipleCoursesEachGetAssignments() {
        when(courseCacheRepository.findByOocUser(testUser)).thenReturn(List.of());
        when(courseCacheRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
 
        CanvasCourse course1 = buildCanvasCourse(301, "Software Engineering", "CS506");
        CanvasCourse course2 = buildCanvasCourse(302, "Linear Algebra", "MATH340");
 
        CanvasAssignment a1 = buildCanvasAssignment(8001, "Sprint Review", "2026-04-20T23:59:00Z");
        CanvasAssignment a2 = buildCanvasAssignment(8002, "Problem Set 5", "2026-04-22T23:59:00Z");
 
        Map<Long, List<CanvasAssignment>> assignments = Map.of(
            301L, List.of(a1),
            302L, List.of(a2)
        );
 
        syncService.replaceForUser(testUser, List.of(course1, course2), assignments);
 
        verify(courseCacheRepository, times(2)).save(any(CourseCache.class));
        verify(assignmentCacheRepository, times(2)).save(any(AssignmentCache.class));
    }
 
    /**
     * A course with no assignments in the map should produce no assignment saves.
     */
    @Test
    void courseWithNoAssignments() {
        when(courseCacheRepository.findByOocUser(testUser)).thenReturn(List.of());
        when(courseCacheRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
 
        CanvasCourse course = buildCanvasCourse(303, "Dance 132", "DANCE132");

        syncService.replaceForUser(testUser, List.of(course), Collections.emptyMap());
 
        verify(courseCacheRepository).save(any(CourseCache.class));
        verify(assignmentCacheRepository, never()).save(any());
    }
 
    // Due date parsing tests

    /**
     * Canvas returns due_at as a full ISO 8601 timestamp. must parse to LocalDateTime correctly
     */
    @Test
    void timestampDueDateParsedCorrectly() {
        when(courseCacheRepository.findByOocUser(testUser)).thenReturn(List.of());
        when(courseCacheRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
 
        CanvasCourse course = buildCanvasCourse(304, "Test Course", "TEST101");
        CanvasAssignment assignment = buildCanvasAssignment(9001, "Midterm", "2026-04-22T23:59:00Z");
        Map<Long, List<CanvasAssignment>> assignments = Map.of(304L, List.of(assignment));
 
        syncService.replaceForUser(testUser, List.of(course), assignments);
 
        ArgumentCaptor<AssignmentCache> captor = ArgumentCaptor.forClass(AssignmentCache.class);
        verify(assignmentCacheRepository).save(captor.capture());
        LocalDateTime due = captor.getValue().getDueDate();
 
        assertNotNull(due);
        assertEquals(2026, due.getYear());
        assertEquals(4, due.getMonthValue());
        assertEquals(22, due.getDayOfMonth());
    }
 
    // Helper builders
 
    private CourseCache buildCourseCache(OocUser user, int canvasCourseId, String name, String code) {
        CourseCache cc = new CourseCache();
        cc.setOocUser(user);
        cc.setCanvasCourseId(canvasCourseId);
        cc.setCourseName(name);
        cc.setCourseCode(code);
        return cc;
    }
 
    private CanvasCourse buildCanvasCourse(long id, String name, String code) {
        CanvasCourse c = new CanvasCourse();
        c.setId(id);
        c.setName(name);
        c.setCourseCode(code);
        return c;
    }
 
    private CanvasAssignment buildCanvasAssignment(long id, String name, String dueAt) {
        CanvasAssignment a = new CanvasAssignment();
        a.setId(id);
        a.setName(name);
        a.setDueAt(dueAt);
        return a;
    }
}