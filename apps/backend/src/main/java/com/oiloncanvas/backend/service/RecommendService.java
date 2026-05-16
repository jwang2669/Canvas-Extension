package com.oiloncanvas.backend.service;

import com.oiloncanvas.backend.dto.RecommendRequest;
import com.oiloncanvas.backend.dto.RecommendResponse;
import org.springframework.stereotype.Service;

/**
 * Placeholder for "what should I work on now?" recommendation logic
 * (e.g. due-soon-first, rule-based or LLM).
 * Will be used by RecommendController for POST /api/recommend.
 */
@Service
public class RecommendService {

  /**
   * Recommend what a student should work on next.
   *
   * <p>Current behavior: placeholder only (returns an empty response).
   * Future behavior: rank suggested tasks by urgency/workload context.</p>
   *
   * @param request recommendation request payload
   * @return placeholder RecommendResponse for now
   */
  public RecommendResponse recommend(RecommendRequest request) {
    // Placeholder for future implementation.
    return new RecommendResponse();
  }
}
