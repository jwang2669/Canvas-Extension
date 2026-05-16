package com.oiloncanvas.backend.service;

import com.oiloncanvas.backend.client.OpenAIClient;
import com.oiloncanvas.backend.dto.OpenAIChatRequest;
import com.oiloncanvas.backend.dto.SuggestionRequest;
import com.oiloncanvas.backend.dto.SuggestionResponse;
import com.oiloncanvas.backend.entity.AssignmentCache;
import com.oiloncanvas.backend.entity.CourseCache;
import com.oiloncanvas.backend.entity.OocUser;
import com.oiloncanvas.backend.repository.AssignmentCacheRepository;
import com.oiloncanvas.backend.repository.CourseCacheRepository;
import com.oiloncanvas.backend.repository.OocUserRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

/**
 * Generates suggestions using OpenAI (e.g. assignment order, strategy).
 * Delegates to {@link OpenAIClient}; builds prompts from cached enrollment when sessionId is set.
 */
@Service
public class SuggestionService {

  private static final String SYSTEM_BASE =
      "You are a helpful Canvas LMS study assistant. Answer clearly and concisely. Ground your"
          + " answers in the student's enrolled courses and cached assignments when those lists are"
          + " provided. Use assignment titles, course names, and due dates for workload, planning,"
          + " and prioritization. If an assignment has no due date, say so. If lists are missing or"
          + " empty, say you don't have that data and suggest syncing Canvas from the app or"
          + " checking Canvas directly.";

  private final OpenAIClient openAIClient;
  private final OocUserRepository oocUserRepository;
  private final CourseCacheRepository courseCacheRepository;
  private final AssignmentCacheRepository assignmentCacheRepository;

  public SuggestionService(
      OpenAIClient openAIClient,
      OocUserRepository oocUserRepository,
      CourseCacheRepository courseCacheRepository,
      AssignmentCacheRepository assignmentCacheRepository) {
    this.openAIClient = openAIClient;
    this.oocUserRepository = oocUserRepository;
    this.courseCacheRepository = courseCacheRepository;
    this.assignmentCacheRepository = assignmentCacheRepository;
  }

  /**
   * Generate one suggestion from an optional free-form prompt.
   *
   * <p>If prompt is missing/blank, a default prompt is used.</p>
   *
   * @param request suggestion request payload
   * @return AI-generated recommendation text
   */
  public SuggestionResponse generateSuggestion(SuggestionRequest request) {
    String prompt =
        request != null && request.getPrompt() != null && !request.getPrompt().isBlank()
            ? request.getPrompt()
            : "Give some life advice";

    List<OpenAIChatRequest.Message> messages = new ArrayList<>();

    String sessionId = request != null ? request.getSessionId() : null;
    Optional<OocUser> user =
        sessionId != null && !sessionId.isBlank()
            ? oocUserRepository.findByCurrentSessionId(sessionId.trim())
            : oocUserRepository.findTopByOrderBySessionCreatedAtDesc();

    if (user.isPresent()) {
      List<CourseCache> cached = courseCacheRepository.findByOocUser(user.get());
      if (!cached.isEmpty()) {
        StringBuilder context = new StringBuilder(SYSTEM_BASE);

        String displayName = normalizeDisplayName(request != null ? request.getUserDisplayName() : null);
        if (displayName != null) {
          context.append("\n\nStudent display name: ").append(displayName);
        }

        context.append("\n\nEnrolled courses:\n").append(formatCourses(cached));

        List<AssignmentCache> assignments = collectAssignmentsForUserCourses(cached);
        if (!assignments.isEmpty()) {
          context.append("\n\nCached assignments (from Canvas sync; use for deadlines and planning):\n")
              .append(formatAssignments(assignments));
        } else {
          context.append(
              "\n\nCached assignments: none in the current window (or not yet synced). The student"
                  + " may need to refresh enrollment from the extension so assignments load into the"
                  + " server cache.");
        }

        messages.add(new OpenAIChatRequest.Message("system", context.toString()));
      }
    }

    messages.add(new OpenAIChatRequest.Message("user", prompt));
    String recommendation = openAIClient.getChatCompletion(messages);
    return new SuggestionResponse(recommendation);
  }

  private static String formatCourses(List<CourseCache> courses) {
    StringBuilder sb = new StringBuilder();
    for (CourseCache c : courses) {
      sb.append("- ");
      sb.append(c.getCourseName());
      sb.append(" (");
      sb.append(c.getCourseCode());
      sb.append(")\n");
    }
    return sb.toString().trim();
  }

  /** Days ahead (from today) to include dated assignments in the chat context. */
  private static final int ASSIGNMENT_WINDOW_DAYS = 30;

  private static final int MAX_UNDATED_ASSIGNMENTS = 40;

  private List<AssignmentCache> collectAssignmentsForUserCourses(List<CourseCache> courses) {
    LocalDateTime windowStart = LocalDate.now().atStartOfDay();
    LocalDateTime windowEnd = windowStart.plusDays(ASSIGNMENT_WINDOW_DAYS).withHour(23).withMinute(59).withSecond(59);
    LocalDateTime lookbackStart = windowStart.minusDays(14);

    List<AssignmentCache> dated = new ArrayList<>();
    List<AssignmentCache> undated = new ArrayList<>();

    for (CourseCache cc : courses) {
      for (AssignmentCache a : assignmentCacheRepository.findByCourseCache(cc)) {
        LocalDateTime due = a.getDueDate();
        if (due == null) {
          undated.add(a);
        } else if (!due.isBefore(lookbackStart) && !due.isAfter(windowEnd)) {
          dated.add(a);
        }
      }
    }

    dated.sort(Comparator.comparing(AssignmentCache::getDueDate));
    if (undated.size() > MAX_UNDATED_ASSIGNMENTS) {
      undated = new ArrayList<>(undated.subList(0, MAX_UNDATED_ASSIGNMENTS));
    }

    List<AssignmentCache> merged = new ArrayList<>(dated);
    merged.addAll(undated);
    return merged;
  }

  private static String normalizeDisplayName(String value) {
    if (value == null) {
      return null;
    }
    String t = value.trim();
    return t.isEmpty() ? null : t;
  }

  private static String formatAssignments(List<AssignmentCache> assignments) {
    DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM d, yyyy");
    StringBuilder sb = new StringBuilder();
    for (AssignmentCache a : assignments) {
      sb.append("- ");
      if (a.getCourseCache() != null) {
        sb.append(a.getCourseCache().getCourseName()).append(" — ");
      }
      sb.append(a.getAssignmentName());
      if (a.getDueDate() != null) {
        sb.append(" (due ").append(a.getDueDate().format(fmt)).append(")");
      } else {
        sb.append(" (no due date in cache)");
      }
      sb.append("\n");
    }
    return sb.toString().trim();
  }
}
