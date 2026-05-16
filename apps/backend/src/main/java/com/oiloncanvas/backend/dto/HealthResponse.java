package com.oiloncanvas.backend.dto;

/**
 * DTO for the /api/health endpoint response.
 */
public class HealthResponse {

    /** Service health status (for example: "up"). */
    private final String status;
    /** Application/service name. */
    private final String application;

    public HealthResponse(String status, String application) {
        this.status = status;
        this.application = application;
    }

    public String getStatus() {
        return status;
    }

    public String getApplication() {
        return application;
    }
}
