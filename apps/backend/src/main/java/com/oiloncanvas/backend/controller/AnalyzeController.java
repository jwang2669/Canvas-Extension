package com.oiloncanvas.backend.controller;

import com.oiloncanvas.backend.dto.AnalyzeRequest;
import com.oiloncanvas.backend.dto.AnalyzeResponse;
import com.oiloncanvas.backend.service.AnalyzeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST API for workload analysis: POST /api/analyze.
 * Delegates to {@link com.oiloncanvas.backend.service.AnalyzeService}.
 */
@RestController
@RequestMapping("/api/analyze")
public class AnalyzeController {

  private final AnalyzeService analyzeService;

  public AnalyzeController(AnalyzeService analyzeService) {
    this.analyzeService = analyzeService;
  }

  /**
   * Analyzes weekly content/workload input.
   *
   * @param request analyze request payload
   * @return analyze response payload
   */
  @PostMapping
  public ResponseEntity<AnalyzeResponse> analyze(@RequestBody AnalyzeRequest request) {
    AnalyzeResponse response = analyzeService.analyze(request);
    return ResponseEntity.ok(response);
  }
}
