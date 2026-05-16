package com.oiloncanvas.backend.repository;

import com.oiloncanvas.backend.entity.AssignmentAdditions;
import com.oiloncanvas.backend.entity.AssignmentCache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


/**
 * AssignmentAdditions JPA repository. Defines custom query method to find
 * an assignment's additions by the assignment cache.
 */
@Repository
public interface AssignmentAdditionsRepository extends JpaRepository<AssignmentAdditions, Integer> {
    // find assignment addition by assignment cache
    AssignmentAdditions findByAssignmentCache(AssignmentCache assignmentCache);
    
    // save, findById, findAll, deleteById, etc. are inherited and will be used by service.
}
