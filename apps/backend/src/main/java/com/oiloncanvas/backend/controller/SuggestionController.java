package com.oiloncanvas.backend.controller;

import com.oiloncanvas.backend.dto.SuggestionRequest;
import com.oiloncanvas.backend.dto.SuggestionResponse;
import com.oiloncanvas.backend.service.SuggestionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST API for AI suggestions: POST /api/suggestions.
 * Delegates to {@link SuggestionService}.
 */
@RestController
@RequestMapping("/api/suggestions")
public class SuggestionController {

  private final SuggestionService suggestionService;

  public SuggestionController(SuggestionService suggestionService) {
    this.suggestionService = suggestionService;
  }

  /**
   * Generates an AI suggestion from the provided prompt.
   *
   * @param request suggestion request payload
   * @return AI suggestion response payload
   */
  @PostMapping
  public ResponseEntity<SuggestionResponse> getSuggestion(@RequestBody SuggestionRequest request) {
    SuggestionResponse response = suggestionService.generateSuggestion(request);
    return ResponseEntity.ok(response);
  }
}
