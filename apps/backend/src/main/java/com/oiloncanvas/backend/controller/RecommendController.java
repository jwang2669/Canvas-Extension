package com.oiloncanvas.backend.controller;

import com.oiloncanvas.backend.dto.RecommendRequest;
import com.oiloncanvas.backend.dto.RecommendResponse;
import com.oiloncanvas.backend.service.RecommendService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST API for "what should I work on now?": POST /api/recommend.
 * Delegates to {@link com.oiloncanvas.backend.service.RecommendService}.
 */
@RestController
@RequestMapping("/api/recommend")
public class RecommendController {

  private final RecommendService recommendService;

  public RecommendController(RecommendService recommendService) {
    this.recommendService = recommendService;
  }

  /**
   * Returns recommendation data for what to work on next.
   *
   * @param request recommendation request payload
   * @return recommendation response payload
   */
  @PostMapping
  public ResponseEntity<RecommendResponse> recommend(@RequestBody RecommendRequest request) {
    RecommendResponse response = recommendService.recommend(request);
    return ResponseEntity.ok(response);
  }
}
