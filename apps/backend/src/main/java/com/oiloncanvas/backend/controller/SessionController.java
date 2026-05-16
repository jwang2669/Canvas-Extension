package com.oiloncanvas.backend.controller;

import com.oiloncanvas.backend.dto.SessionRequest;
import com.oiloncanvas.backend.dto.SessionResponse;
import com.oiloncanvas.backend.service.SessionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST API for session/context: POST /api/sessions (start user/course context).
 * Delegates to {@link SessionService}.
 */
@RestController
@RequestMapping("/api/sessions")
public class SessionController {

  private final SessionService sessionService;

  public SessionController(SessionService sessionService) {
    this.sessionService = sessionService;
  }

  /**
   * Starts session context for a user and selected courses.
   *
   * @param request session request payload
   * @return session response payload
   */
  @PostMapping
  public ResponseEntity<SessionResponse> startSession(@RequestBody SessionRequest request) {
    SessionResponse response = sessionService.startSession(request);
    return ResponseEntity.ok(response);
  }
}
