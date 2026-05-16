package com.oiloncanvas.backend.service;

import com.oiloncanvas.backend.dto.TaskRequest;
import com.oiloncanvas.backend.dto.TaskResponse;
import com.oiloncanvas.backend.dto.canvasapi.CanvasAssignment;
import com.oiloncanvas.backend.entity.OocUser;
import com.oiloncanvas.backend.entity.TodoItem;
import com.oiloncanvas.backend.repository.OocUserRepository;
import com.oiloncanvas.backend.repository.TodoItemRepository;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.temporal.WeekFields;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

/**
 * Handles creating, reading, and managing tasks (assignments/quizzes).
 * Also converts Canvas API assignment data into our own task format.
 */
@Service
public class TaskService {

  private static final Pattern ISO_WEEK_PATTERN = Pattern.compile("^\\d{4}-W\\d{2}$");
  private static final DateTimeFormatter API_DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
  private final TodoItemRepository todoItemRepository;
  private final OocUserRepository oocUserRepository;

  public TaskService(TodoItemRepository todoItemRepository, OocUserRepository oocUserRepository) {
    this.todoItemRepository = todoItemRepository;
    this.oocUserRepository = oocUserRepository;
  }

  /**
   * Create a task from a manual request (e.g. user-created task).
   *
    * Validates and normalizes request data, persists the task in todo_item,
    * and returns the saved response payload.
   *
    * @param request the task details
   * @return the created TaskResponse
   */
  public TaskResponse createTask(TaskRequest request) {
    if (request == null) {
      throw new IllegalArgumentException("Task request cannot be null.");
    }

    String title = normalizeRequired(request.getTitle(), "title");
    String course = normalizeRequired(request.getCourse(), "course");
    String type = normalizeType(request.getType());
    int estimatedMinutes = normalizeEstimatedMinutes(request.getEstimatedMinutes());
    String dueDate = normalizeOptional(request.getDueDate());

    OocUser owner = resolveOrCreateDefaultUser();

    TodoItem item = new TodoItem();
    item.setOocUser(owner);
    item.setDescription(title);
    item.setTaskType(type);
    item.setEstimatedMinutes(estimatedMinutes);
    item.setCourseCode(course);
    item.setDueDate(parseDueDateTime(dueDate));
    item.setCompleted(false);

    TodoItem saved = todoItemRepository.save(item);

    return new TaskResponse(
      saved.getTodoId().longValue(),
      saved.getDescription(),
      formatDateTime(saved.getDueDate()),
      saved.getTaskType(),
      saved.getEstimatedMinutes() != null ? saved.getEstimatedMinutes() : 0,
      saved.getCourseCode(),
      Boolean.TRUE.equals(saved.getCompleted()) ? "completed" : "pending");
  }

  /**
   * Get all tasks for a given week, passed as a string like "2026-W10".
   *
   * @param week the week to look up, e.g. "2026-W10" means week 10 of 2026
   * @return list of tasks for that week
   */
  public List<TaskResponse> getTasksForWeek(String week) {
    if (!isValidWeek(week)) {
      throw new IllegalArgumentException(
          "Week must use format YYYY-Www, for example 2026-W10.");
    }

    OocUser owner = resolveOrCreateDefaultUser();
    LocalDate weekStart = parseIsoWeekStart(week);
    LocalDateTime rangeStart = weekStart.atStartOfDay();
    LocalDateTime rangeEnd = weekStart.plusDays(6).atTime(23, 59, 59);

    // Returns tasks with non-null due_date values inside this week.
    return todoItemRepository.findByOocUserAndDueDateBetween(owner, rangeStart, rangeEnd)
        .stream()
        .map(this::toTaskResponse)
        .toList();
  }

  /**
   * Convert a Canvas API assignment into the internal format.
   *
    * Uses estimatedMinutes = 0 because Canvas doesn't provide this field.
    * Uses status = "pending" as the default task state.
   *
   * @param ca         the assignment object we got from Canvas
   * @param courseCode the course code to tag this task with, e.g. "CS506-SP26"
   * @return a TaskResponse built from the Canvas data
   */
  public TaskResponse fromCanvasAssignment(CanvasAssignment ca, String courseCode) {
    return new TaskResponse(
        ca.getId(),
        ca.getName(),
        ca.getDueAt(),
        "assignment",
        0,
        courseCode,
        "pending");
  }

  /**
   * Checks whether a week string matches YYYY-Www format.
    *
    * @param week week string to validate
    * @return true when week matches YYYY-Www
   */
  private static boolean isValidWeek(String week) {
    if (week == null) {
      return false;
    }
    return ISO_WEEK_PATTERN.matcher(week.trim()).matches();
  }

  /**
   * Normalizes a required string field and throws if null/blank.
    *
    * @param value raw field value
    * @param fieldName field label for error messages
    * @return normalized non-blank value
   */
  private static String normalizeRequired(String value, String fieldName) {
    String normalized = normalizeOptional(value);
    if (normalized == null) {
      throw new IllegalArgumentException(
          "Task " + fieldName + " cannot be null or blank.");
    }
    return normalized;
  }

  /**
   * Trims an optional string field and converts blank values to null.
    *
    * @param value raw optional value
    * @return trimmed value or null when blank/null
   */
  private static String normalizeOptional(String value) {
    if (value == null) {
      return null;
    }
    String trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }

  /**
   * Normalizes task type and applies a default when missing.
    *
    * @param type raw task type value
    * @return normalized task type, defaulting to "assignment"
   */
  private static String normalizeType(String type) {
    String normalized = normalizeOptional(type);
    return normalized == null ? "assignment" : normalized.toLowerCase();
  }

  /**
   * Prevents negative time estimates.
    *
    * @param estimatedMinutes requested estimate
    * @return non-negative estimate value
   */
  private static int normalizeEstimatedMinutes(int estimatedMinutes) {
    return Math.max(0, estimatedMinutes);
  }

  /**
   * Resolves an owner row for todo items.
   *
   * Creates a default user record when the table is empty so task endpoints
   * still work in a fresh database.
   *
   * @return existing or newly created user row
   */
  private OocUser resolveOrCreateDefaultUser() {
    return oocUserRepository.findAll().stream()
        .findFirst()
        .orElseGet(() -> oocUserRepository.save(new OocUser()));
  }

  /**
   * Parses due date strings that may be full timestamps or local dates.
    *
    * @param dueDate date/time string from request
    * @return parsed LocalDateTime or null when input is null
    * @throws IllegalArgumentException when value is non-null but not parseable
   */
  private static LocalDateTime parseDueDateTime(String dueDate) {
    if (dueDate == null) {
      return null;
    }

    try {
      return OffsetDateTime.parse(dueDate).toLocalDateTime();
    } catch (DateTimeParseException ignored) {
    }

    try {
      return LocalDateTime.parse(dueDate);
    } catch (DateTimeParseException ignored) {
    }

    try {
      return LocalDate.parse(dueDate).atStartOfDay();
    } catch (DateTimeParseException ignored) {
    }

    throw new IllegalArgumentException(
        "Task dueDate must be an ISO date or date-time string.");
  }

  /**
   * Parses an ISO week string (YYYY-Www) and returns that week's Monday.
   *
   * @param week week string in YYYY-Www format
   * @return LocalDate for Monday of that week
   */
  private static LocalDate parseIsoWeekStart(String week) {
    int year = Integer.parseInt(week.substring(0, 4));
    int weekNumber = Integer.parseInt(week.substring(6, 8));

    if (weekNumber < 1 || weekNumber > 53) {
      throw new IllegalArgumentException("Week number must be between 01 and 53.");
    }

    WeekFields iso = WeekFields.ISO;
    return LocalDate.of(year, 1, 4)
        .with(iso.weekBasedYear(), year)
        .with(iso.weekOfWeekBasedYear(), weekNumber)
        .with(iso.dayOfWeek(), 1);
  }

  /**
   * Maps a persisted todo entity to API task response.
   *
   * @param item persisted todo item
   * @return mapped task response
   */
  private TaskResponse toTaskResponse(TodoItem item) {
    return new TaskResponse(
        item.getTodoId().longValue(),
        item.getDescription(),
        formatDateTime(item.getDueDate()),
        item.getTaskType() != null ? item.getTaskType() : "assignment",
        item.getEstimatedMinutes() != null ? item.getEstimatedMinutes() : 0,
        item.getCourseCode(),
        Boolean.TRUE.equals(item.getCompleted()) ? "completed" : "pending");
  }

  /**
   * Formats LocalDateTime values for API responses.
   *
   * @param dateTime date-time value from persistence
   * @return ISO local date-time string or null
   */
  private static String formatDateTime(LocalDateTime dateTime) {
    if (dateTime == null) {
      return null;
    }
    return dateTime.format(API_DATE_TIME_FORMATTER);
  }
}
