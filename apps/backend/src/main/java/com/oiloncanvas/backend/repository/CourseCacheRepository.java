package com.oiloncanvas.backend.repository;

import com.oiloncanvas.backend.entity.CourseCache;
import com.oiloncanvas.backend.entity.OocUser;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


/**
 * CourseCache JPA repository. Defines custom query method to find
 * course cache by an OocUser.
 */
@Repository
public interface CourseCacheRepository extends JpaRepository<CourseCache, Integer> {
    // find course cache by ooc user
    List<CourseCache> findByOocUser(OocUser oocUser);
    
    // save, findById, findAll, deleteById, etc. are inherited and will be used by service.
}