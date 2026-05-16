package com.oiloncanvas.backend.dto;

import com.oiloncanvas.backend.dto.canvasapi.CanvasUser;

/**
 * API response for {@code GET /api/canvas/me}: Canvas profile for the configured access token
 * ({@code GET /api/v1/users/self}).
 */
public class CanvasUserProfileResponse {

  private final CanvasUser user;

  public CanvasUserProfileResponse(CanvasUser user) {
    this.user = user;
  }

  public CanvasUser getUser() {
    return user;
  }
}
