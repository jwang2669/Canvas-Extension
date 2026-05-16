package com.oiloncanvas.backend.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.oiloncanvas.backend.entity.AssignmentCache;
import com.oiloncanvas.backend.entity.CourseCache;
import com.oiloncanvas.backend.entity.OocUser;
import com.oiloncanvas.backend.repository.AssignmentCacheRepository;
import com.oiloncanvas.backend.repository.CourseCacheRepository;
import com.oiloncanvas.backend.repository.OocUserRepository;
import java.time.DayOfWeek;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestPropertySource(properties = {
    "openai.api.key=test-key",
    "spring.application.name=oil-on-canvas-backend-test"
})
class CanvasCourseControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OocUserRepository oocUserRepository;

    @Autowired
    private CourseCacheRepository courseCacheRepository;

    @Autowired
    private AssignmentCacheRepository assignmentCacheRepository;

    /**
     * Verifies the current-week assignments endpoint excludes next-week records.
     *
     * @throws Exception when MockMvc request execution fails
     */
    @Test
    void getAssignments_withCurrentWeek_returnsOnlyCurrentWeekAssignments() throws Exception {
        OocUser user = new OocUser();
        user = oocUserRepository.save(user);

        CourseCache course = new CourseCache();
        course.setOocUser(user);
        course.setCanvasCourseId(101);
        course.setCourseName("Software Engineering");
        course.setCourseCode("CS506");
        course = courseCacheRepository.save(course);

        LocalDate today = LocalDate.now();
        LocalDate currentWeekStart = today.with(java.time.temporal.TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate nextWeekStart = today.with(java.time.temporal.TemporalAdjusters.next(DayOfWeek.MONDAY));

        AssignmentCache currentWeek = new AssignmentCache();
        currentWeek.setCourseCache(course);
        currentWeek.setCanvasAssignmentId(5001);
        currentWeek.setAssignmentName("Current Week Task");
        currentWeek.setDueDate(currentWeekStart.plusDays(2).atTime(12, 0));

        AssignmentCache nextWeek = new AssignmentCache();
        nextWeek.setCourseCache(course);
        nextWeek.setCanvasAssignmentId(5002);
        nextWeek.setAssignmentName("Next Week Task");
        nextWeek.setDueDate(nextWeekStart.plusDays(2).atTime(12, 0));

        assignmentCacheRepository.save(currentWeek);
        assignmentCacheRepository.save(nextWeek);

        mockMvc.perform(get("/api/canvas/assignments").param("week", "current"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.assignments[0].title").value("Current Week Task"));
    }
}
