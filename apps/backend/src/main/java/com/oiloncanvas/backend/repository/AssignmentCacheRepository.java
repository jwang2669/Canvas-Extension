package com.oiloncanvas.backend.repository;

import com.oiloncanvas.backend.entity.AssignmentCache;
import com.oiloncanvas.backend.entity.CourseCache;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


/**
 * AssignmentCache JPA repository. Defines custom query methods to find
 * assignment caches by course cache, or an assignment's cache by its
 * canvas assignment id.
 */
@Repository
public interface AssignmentCacheRepository extends JpaRepository<AssignmentCache, Integer> {
    // find assignment cache by course cache
    List<AssignmentCache> findByCourseCache(CourseCache courseCache);

    // find a particular assignment's cache by it's canvas assignment id
    AssignmentCache findByCanvasAssignmentId(Integer canvasAssignmentId);

    // find assignments with due dates in a given range
    List<AssignmentCache> findByDueDateBetween(LocalDateTime start, LocalDateTime end);
    
    // save, findById, findAll, deleteById, etc. are inherited and will be used by service.
}