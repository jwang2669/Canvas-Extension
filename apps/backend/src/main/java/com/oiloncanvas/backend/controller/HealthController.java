package com.oiloncanvas.backend.controller;

import com.oiloncanvas.backend.dto.HealthResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exposes a simple health/info endpoint for availability checks and developer convenience.
 */
@RestController
@RequestMapping("/api")
public class HealthController {

    private final String applicationName;

    public HealthController(@Value("${spring.application.name}") String applicationName) {
        this.applicationName = applicationName;
    }

    /**
     * Health/info endpoint for quick backend availability checks.
     *
     * @return service status and application name
     */
    @GetMapping("/health")
    public ResponseEntity<HealthResponse> health() {
        return ResponseEntity.ok(new HealthResponse("up", applicationName));
    }
}
