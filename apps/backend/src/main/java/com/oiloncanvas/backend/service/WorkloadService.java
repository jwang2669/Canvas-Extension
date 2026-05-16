package com.oiloncanvas.backend.service;

import com.oiloncanvas.backend.client.OpenAIClient;
import com.oiloncanvas.backend.dto.TaskItem;
import com.oiloncanvas.backend.dto.WorkloadRequest;
import com.oiloncanvas.backend.dto.WorkloadResponse;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/**
 * Workload scoring for a given week: total estimated_minutes per day/week and
 * due-date proximity weighting (tasks closer to due date count more).
 * Uses OpenAI to generate a short natural-language summary of the workload.
 */
@Service
public class WorkloadService {

  private static final List<String> DAY_NAMES =
      List.of(
          "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday");

  private final OpenAIClient openAIClient;

  /**
   * Create a workload service backed by OpenAI for natural-language summaries.
   *
   * @param openAIClient outbound OpenAI HTTP client
   */
  public WorkloadService(OpenAIClient openAIClient) {
    this.openAIClient = openAIClient;
  }

  /**
   * Computes per-day and total minutes, applies due-date proximity weighting, and
   * generates an AI summary (e.g. "Tue/Thu look heavy, start Project early").
   */
  public WorkloadResponse computeWorkload(WorkloadRequest request) {
    LocalDate ref = parseReferenceDate(request != null ? request.getReferenceDate() : null);
    List<TaskItem> tasks = request != null && request.getTasks() != null ? request.getTasks() : List.of();

    Map<String, Integer> perDayMinutes = initPerDay();
    Map<String, Double> weightedPerDay = initWeightedPerDay();
    int totalMinutes = 0;

    for (TaskItem task : tasks) {
      if (task.getEstimatedMinutes() <= 0) continue;
      LocalDate due = parseDueDate(task.getDueDate());
      if (due == null) continue;

      String dayKey = dayOfWeekName(due);
      perDayMinutes.merge(dayKey, task.getEstimatedMinutes(), Integer::sum);
      totalMinutes += task.getEstimatedMinutes();

      long daysUntilDue = java.time.temporal.ChronoUnit.DAYS.between(ref, due);
      double weight = dueDateProximityWeight(daysUntilDue);
      weightedPerDay.merge(dayKey, task.getEstimatedMinutes() * weight, Double::sum);
    }

    String summary = generateSummary(perDayMinutes, totalMinutes, weightedPerDay, request != null ? request.getWeek() : null);
    return new WorkloadResponse(perDayMinutes, totalMinutes, weightedPerDay, summary);
  }

  private static Map<String, Integer> initPerDay() {
    return DAY_NAMES.stream().collect(Collectors.toMap(d -> d, d -> 0, (a, b) -> a, LinkedHashMap::new));
  }

  private static Map<String, Double> initWeightedPerDay() {
    return DAY_NAMES.stream().collect(Collectors.toMap(d -> d, d -> 0.0, (a, b) -> a, LinkedHashMap::new));
  }

  private static LocalDate parseReferenceDate(String s) {
    if (s == null || s.isBlank()) return LocalDate.now();
    try {
      return LocalDate.parse(s);
    } catch (DateTimeParseException e) {
      return LocalDate.now();
    }
  }

  private static LocalDate parseDueDate(String s) {
    if (s == null || s.isBlank()) return null;
    try {
      return LocalDate.parse(s);
    } catch (DateTimeParseException e) {
      return null;
    }
  }

  private static String dayOfWeekName(LocalDate date) {
    return date.getDayOfWeek().toString().charAt(0) + date.getDayOfWeek().toString().substring(1).toLowerCase();
  }

  /**
   * Weight by due-date proximity: tasks due sooner count more.
   * weight = 1 + 1 / (daysUntilDue + 1), so e.g. due today=2, due in 1 day=1.5, due in 6 days≈1.14.
   */
  private static double dueDateProximityWeight(long daysUntilDue) {
    if (daysUntilDue < 0) return 0.5; // overdue: still count but less
    return 1.0 + 1.0 / (daysUntilDue + 1.0);
  }

  /**
   * Build the final prompt and request a short student-facing summary from OpenAI.
   *
   * @param perDayMinutes raw per-day minute totals
   * @param totalMinutes total minutes across all tasks
   * @param weightedPerDay urgency-weighted per-day scores
   * @param week optional week label for context
   * @return one short summary string; fallback text on client error
   */
  private String generateSummary(
      Map<String, Integer> perDayMinutes,
      int totalMinutes,
      Map<String, Double> weightedPerDay,
      String week) {
    StringBuilder sb = new StringBuilder();
    sb.append("Workload for this week");
    if (week != null && !week.isBlank()) sb.append(" (").append(week).append(")");
    sb.append(": total ").append(totalMinutes).append(" estimated minutes. ");
    sb.append("Per day (raw minutes): ");
    perDayMinutes.forEach((day, mins) -> { if (mins > 0) sb.append(day).append(" ").append(mins).append(" min; "); });
    sb.append("Weighted by due-date proximity (higher = more urgent): ");
    weightedPerDay.forEach((day, w) -> { if (w > 0) sb.append(day).append(" ").append(String.format("%.1f", w)).append("; "); });
    sb.append("Give one short natural-language summary (1-2 sentences) for a student: which days look heavy, and one concrete tip (e.g. start X early). No preamble.");

    try {
      return openAIClient.getSuggestion(sb.toString());
    } catch (Exception e) {
      return "Summary unavailable. Check OpenAI API key.";
    }
  }
}
