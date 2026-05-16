package com.oiloncanvas.backend.repository;

import com.oiloncanvas.backend.entity.AssignmentCache;
import com.oiloncanvas.backend.entity.CourseCache;
import com.oiloncanvas.backend.entity.OocUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import java.time.LocalDateTime;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests database queries to ensure expected data is fetched.
 */
@DataJpaTest
class AssignmentCacheRepositoryTest {

    @Autowired private TestEntityManager entityManager;
    @Autowired private AssignmentCacheRepository assignmentCacheRepository;

    private static final LocalDateTime WEEK_START = LocalDateTime.of(2024, 1, 1, 0, 0, 0);
    private static final LocalDateTime WEEK_END   = LocalDateTime.of(2024, 1, 7, 23, 59, 59);

    private CourseCache testCourse;

    @BeforeEach
    void setUp() {
        OocUser user = new OocUser();
        entityManager.persist(user);
        testCourse = new CourseCache();
        testCourse.setOocUser(user);
        testCourse.setCanvasCourseId(500);
        testCourse.setCourseName("Test Course");
        testCourse.setCourseCode("TEST101");
        entityManager.persist(testCourse);
        entityManager.flush();
    }

    @Test
    void returnsAssignmentsInRange() {
        entityManager.persist(buildAssignment("Due mid-week", WEEK_START.plusDays(2)));
        entityManager.flush();

        List<AssignmentCache> results = assignmentCacheRepository
            .findByDueDateBetween(WEEK_START, WEEK_END);

        assertEquals(1, results.size());
        assertEquals("Due mid-week", results.get(0).getAssignmentName());
    }

    @Test
    void excludesAssignmentsOutsideRange() {
        entityManager.persist(buildAssignment("Due before range", WEEK_START.minusDays(1)));
        entityManager.persist(buildAssignment("Due after range",  WEEK_END.plusDays(1)));
        entityManager.flush();

        List<AssignmentCache> results = assignmentCacheRepository
            .findByDueDateBetween(WEEK_START, WEEK_END);

        assertTrue(results.isEmpty());
    }

    @Test
    void excludesNullDueDates() {
        entityManager.persist(buildAssignment("No due date", null));
        entityManager.flush();

        List<AssignmentCache> results = assignmentCacheRepository
            .findByDueDateBetween(WEEK_START, WEEK_END);

        assertTrue(results.isEmpty());
    }

    @Test
    void inclusiveOnBothBoundaries() {
        entityManager.persist(buildAssignment("Due at week start", WEEK_START));
        entityManager.persist(buildAssignment("Due at week end",   WEEK_END));
        entityManager.flush();

        List<AssignmentCache> results = assignmentCacheRepository
            .findByDueDateBetween(WEEK_START, WEEK_END);

        assertEquals(2, results.size());
    }

    @Test
    void multipleAssignmentsInWeek() {
        entityManager.persist(buildAssignment("Due day 1", WEEK_START.plusDays(1)));
        entityManager.persist(buildAssignment("Due day 3", WEEK_START.plusDays(3)));
        entityManager.persist(buildAssignment("Due day 5", WEEK_START.plusDays(5)));
        entityManager.flush();

        List<AssignmentCache> results = assignmentCacheRepository
            .findByDueDateBetween(WEEK_START, WEEK_END);

        assertEquals(3, results.size());
    }

    @Test
    void noAssignmentsReturnsEmptyList() {
        List<AssignmentCache> results = assignmentCacheRepository
            .findByDueDateBetween(WEEK_START, WEEK_END);

        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    void mixOfInAndOutOfRangeReturnsOnlyInRange() {
        entityManager.persist(buildAssignment("In range",     WEEK_START.plusDays(2)));
        entityManager.persist(buildAssignment("Out of range", WEEK_END.plusDays(2)));
        entityManager.persist(buildAssignment("No due date",  null));
        entityManager.flush();

        List<AssignmentCache> results = assignmentCacheRepository
            .findByDueDateBetween(WEEK_START, WEEK_END);

        assertEquals(1, results.size());
        assertEquals("In range", results.get(0).getAssignmentName());
    }

    @Test
    void nextWeekRangeDoesNotReturnCurrentWeek() {
        LocalDateTime nextWeekStart = WEEK_END.plusSeconds(1);
        LocalDateTime nextWeekEnd   = WEEK_END.plusDays(7);

        entityManager.persist(buildAssignment("Current week", WEEK_START.plusDays(2)));
        entityManager.persist(buildAssignment("Next week",    nextWeekStart.plusDays(2)));
        entityManager.flush();

        List<AssignmentCache> results = assignmentCacheRepository
            .findByDueDateBetween(nextWeekStart, nextWeekEnd);

        assertEquals(1, results.size());
        assertEquals("Next week", results.get(0).getAssignmentName());
    }

    @Test
    void returnsAllAssignmentsForCourse() {
        entityManager.persist(buildAssignment("HW 1", WEEK_START.plusDays(1)));
        entityManager.persist(buildAssignment("HW 2", WEEK_START.plusDays(3)));
        entityManager.persist(buildAssignment("Quiz with no date", null));
        entityManager.flush();

        List<AssignmentCache> results = assignmentCacheRepository
            .findByCourseCache(testCourse);

        assertEquals(3, results.size());
    }

    @Test
    void emptyCourseReturnsEmptyList() {
        List<AssignmentCache> results = assignmentCacheRepository
            .findByCourseCache(testCourse);

        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    void doesNotReturnOtherCoursesAssignments() {
        OocUser otherUser = new OocUser();
        entityManager.persist(otherUser);
        CourseCache otherCourse = new CourseCache();
        otherCourse.setOocUser(otherUser);
        otherCourse.setCanvasCourseId(999);
        otherCourse.setCourseName("Other Course");
        otherCourse.setCourseCode("OTHER999");
        entityManager.persist(otherCourse);

        // one assignment for testCourse one for otherCourse
        entityManager.persist(buildAssignment("My course assignment", WEEK_START.plusDays(1)));
        AssignmentCache other = new AssignmentCache();
        other.setCourseCache(otherCourse);
        other.setCanvasAssignmentId(9999);
        other.setAssignmentName("Other course assignment");
        other.setDueDate(WEEK_START.plusDays(2));
        entityManager.persist(other);
        entityManager.flush();

        List<AssignmentCache> results = assignmentCacheRepository
            .findByCourseCache(testCourse);

        assertEquals(1, results.size());
        assertEquals("My course assignment", results.get(0).getAssignmentName());
    }

    private AssignmentCache buildAssignment(String name, LocalDateTime dueDate) {
        AssignmentCache a = new AssignmentCache();
        a.setCourseCache(testCourse);
        a.setCanvasAssignmentId((int)(Math.random() * 100000));
        a.setAssignmentName(name);
        a.setDueDate(dueDate);
        return a;
    }
}