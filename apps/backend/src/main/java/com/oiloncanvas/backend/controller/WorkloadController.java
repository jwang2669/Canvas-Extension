package com.oiloncanvas.backend.controller;

import com.oiloncanvas.backend.dto.WorkloadRequest;
import com.oiloncanvas.backend.dto.WorkloadResponse;
import com.oiloncanvas.backend.service.WorkloadService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST API for workload scoring and AI summary: POST /api/workload.
 * Accepts a list of tasks (title, dueDate, estimatedMinutes), returns per-day and total
 * minutes, due-date weighted scores, and a short natural-language summary.
 */
@RestController
@RequestMapping("/api/workload")
public class WorkloadController {

  private final WorkloadService workloadService;

  public WorkloadController(WorkloadService workloadService) {
    this.workloadService = workloadService;
  }

  /**
   * Computes workload metrics and a short AI summary for the given task list.
   *
   * @param request workload request payload
   * @return workload scoring and summary response
   */
  @PostMapping
  public ResponseEntity<WorkloadResponse> computeWorkload(@RequestBody WorkloadRequest request) {
    WorkloadResponse response = workloadService.computeWorkload(request);
    return ResponseEntity.ok(response);
  }
}
