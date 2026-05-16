package com.oiloncanvas.backend.repository;

import com.oiloncanvas.backend.entity.CanvasInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


/**
 * CanvasInstance JPA repository. Defines custom query method to find
 * canvas instance by its URL.
 */
@Repository
public interface CanvasInstanceRepository extends JpaRepository<CanvasInstance, Integer> {
    // find a canvas instance by its URL (property name matches entity getter getBaseURL → baseURL)
    CanvasInstance findByBaseURL(String baseURL);

    // save, findById, findAll, deleteById, etc. are inherited and will be used by service.
}