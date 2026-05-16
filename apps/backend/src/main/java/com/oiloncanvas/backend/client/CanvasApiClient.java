package com.oiloncanvas.backend.client;

import com.oiloncanvas.backend.dto.canvasapi.CanvasCourse;
import com.oiloncanvas.backend.dto.canvasapi.CanvasUser;
import com.oiloncanvas.backend.dto.canvasapi.CanvasAssignment;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

/**
 * Outbound HTTP client for the Canvas LMS API.
 * Uses the single configured access token (CANVAS_ACCESS_TOKEN) and base URL (CANVAS_BASE_URL).
 * This bean is only created when both are set; otherwise it is not registered.
 */
@Component
@ConditionalOnExpression(
    "!'${canvas.base.url:}'.isBlank() && !'${canvas.access.token:}'.isBlank()")
public class CanvasApiClient {

  private final WebClient webClient;

  public CanvasApiClient(@Qualifier("canvasWebClient") WebClient webClient) {
    this.webClient = webClient;
  }

  /**
   * Fetches the current user (profile) for the configured access token.
   *
   * @return Canvas user profile, or null if the request fails (e.g. 401, network)
   */
  public CanvasUser getCurrentUser() {
    try {
      return webClient
          .get()
          .uri("/api/v1/users/self")
          .retrieve()
          .bodyToMono(CanvasUser.class)
          .block();
    } catch (WebClientResponseException e) {
      return null;
    }
  }

  /**
   * Fetches courses where the current user has an active enrollment (still enrolled, not
   * completed/concluded from Canvas's perspective). Uses pagination size 100 per request.
   *
   * @return list of courses (may be empty), or empty list if the request fails
   * @see <a href="https://canvas.instructure.com/doc/api/courses.html">Canvas Courses API</a>
   */
  public List<CanvasCourse> getCoursesWithActiveEnrollment() {
    try {
      List<CanvasCourse> list =
          webClient
              .get()
              .uri(
                  uriBuilder ->
                      uriBuilder
                          .path("/api/v1/courses")
                          .queryParam("enrollment_state", "active")
                          .queryParam("per_page", 100)
                          .build())
              .retrieve()
              .bodyToMono(new ParameterizedTypeReference<List<CanvasCourse>>() {})
              .block();
      return list != null ? list : Collections.emptyList();
    } catch (WebClientResponseException e) {
      return Collections.emptyList();
    }
  }

  /**
   * Fetches assignments for a given course the user is actively enrolled in.
   * 
   * @return list of course assignments or empty list if request fails
   */
  public List<CanvasAssignment> getAssignmentsForCourse(int courseId) {
    List<CanvasAssignment> all = new ArrayList<>();
    String nextUrl = "/api/v1/courses/" + courseId + "/assignments?per_page=100&include[]=overrides";
    try {
      while(nextUrl != null) {
        String currUrl = nextUrl;
        nextUrl = null;

        ResponseEntity<List<CanvasAssignment>> response = 
          webClient
            .get()
            .uri(currUrl)
            .retrieve()
            .toEntity(new ParameterizedTypeReference<List<CanvasAssignment>>() {})
            .block();

        if (response == null) {
          break;
        }

        List<CanvasAssignment> page = response.getBody();
        if (page != null) {
          all.addAll(page);
        }

        String linkHeader = response.getHeaders().getFirst("Link");
        if (linkHeader != null) {
          for (String part : linkHeader.split(",")) {
            part = part.trim();
            if (part.endsWith("rel=\"next\"")) {
              int start = part.indexOf('<') + 1;
              int end = part.indexOf('>');
              if (start > 0 && end > start) {
                nextUrl = part.substring(start, end);
              }
              break;
            }
          }
        }
      }
    } catch (WebClientResponseException e) {
      return Collections.emptyList();
    }
    return all;
  }
}
