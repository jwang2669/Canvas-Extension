package com.oiloncanvas.backend.service;

import com.oiloncanvas.backend.dto.AnalyzeRequest;
import com.oiloncanvas.backend.dto.AnalyzeResponse;
import org.springframework.stereotype.Service;

/**
 * Placeholder for workload analysis (e.g. workload score, heavy-day detection).
 * Will be used by AnalyzeController for POST /api/analyze.
 */
@Service
public class AnalyzeService {

  /**
   * Analyze weekly content/workload input.
   *
   * <p>Current behavior: placeholder only (returns an empty response).
   * Future behavior: compute total load, heavy days, and summary text.</p>
   *
   * @param request analyze payload from the client
   * @return placeholder AnalyzeResponse for now
   */
  public AnalyzeResponse analyze(AnalyzeRequest request) {
    // Placeholder for future implementation.
    return new AnalyzeResponse();
  }
}
