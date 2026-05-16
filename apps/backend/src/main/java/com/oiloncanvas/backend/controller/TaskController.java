package com.oiloncanvas.backend.controller;

import com.oiloncanvas.backend.dto.TaskRequest;
import com.oiloncanvas.backend.dto.TaskResponse;
import com.oiloncanvas.backend.service.TaskService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST API for tasks: POST /api/tasks, GET /api/tasks?week=...
 * Delegates to {@link com.oiloncanvas.backend.service.TaskService}.
 */
@RestController
@RequestMapping("/api/tasks")
public class TaskController {

  private final TaskService taskService;

  public TaskController(TaskService taskService) {
    this.taskService = taskService;
  }

  /**
   * Creates a task from client-provided task details.
   *
   * @param request task creation payload
   * @return created task payload
   */
  @PostMapping
  public ResponseEntity<TaskResponse> createTask(@RequestBody TaskRequest request) {
    TaskResponse response = taskService.createTask(request);
    return ResponseEntity.ok(response);
  }

  /**
   * Returns tasks for the requested week.
   *
   * @param week optional week string (for example, "2026-W10")
   * @return list of tasks for that week
   */
  @GetMapping
  public ResponseEntity<List<TaskResponse>> getTasksForWeek(
      @RequestParam(value = "week", required = false) String week) {
    List<TaskResponse> tasks = taskService.getTasksForWeek(week);
    return ResponseEntity.ok(tasks);
  }
}
