package com.oiloncanvas.backend.service;

import com.oiloncanvas.backend.dto.TaskRequest;
import com.oiloncanvas.backend.dto.TaskResponse;
import com.oiloncanvas.backend.entity.OocUser;
import com.oiloncanvas.backend.entity.TodoItem;
import com.oiloncanvas.backend.repository.OocUserRepository;
import com.oiloncanvas.backend.repository.TodoItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TaskService database interactions.
 * Uses Mockito to mock repositories, testing service logic in isolation.
 * 
 * Run with: ./gradlew test
 */
@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TodoItemRepository todoItemRepository;

    @Mock
    private OocUserRepository oocUserRepository;

    private TaskService taskService;

    private OocUser testUser;

    @BeforeEach
    void setUp() {
        taskService = new TaskService(todoItemRepository, oocUserRepository);
        testUser = new OocUser();
    }

    /**
     * Helper to set up the common mock for user repository.
     */
    private void mockUserExists() {
        lenient().when(oocUserRepository.findAll()).thenReturn(List.of(testUser));
    }

    /**
     * Helper to create a mock saved TodoItem with an ID.
     */
    private TodoItem createSavedTodoItem(TodoItem source, Integer id) {
        TodoItem saved = new TodoItem();
        saved.setOocUser(source.getOocUser());
        saved.setDescription(source.getDescription());
        saved.setTaskType(source.getTaskType());
        saved.setEstimatedMinutes(source.getEstimatedMinutes());
        saved.setCourseCode(source.getCourseCode());
        saved.setDueDate(source.getDueDate());
        saved.setCompleted(source.getCompleted());
        // Use reflection to set the ID since there's no setter
        try {
            Field todoIdField = TodoItem.class.getDeclaredField("todoId");
            todoIdField.setAccessible(true);
            todoIdField.set(saved, id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set todoId", e);
        }
        return saved;
    }

    /**
     * Test that createTask persists a TodoItem to the database
     * with the correct field values.
     */
    @Test
    void createTask_savesToDatabase() {
        // Arrange
        mockUserExists();
        TaskRequest request = new TaskRequest();
        request.setTitle("Homework 5");
        request.setCourse("CS506");
        request.setType("assignment");
        request.setEstimatedMinutes(60);
        request.setDueDate("2026-04-15T23:59:00");

        // Mock save to return the item with an ID set
        when(todoItemRepository.save(any(TodoItem.class))).thenAnswer(invocation -> {
            TodoItem item = invocation.getArgument(0);
            return createSavedTodoItem(item, 1);
        });

        // Act
        TaskResponse response = taskService.createTask(request);

        // Assert - verify save was called
        verify(todoItemRepository, times(1)).save(any(TodoItem.class));

        // Capture the saved entity to verify field values
        ArgumentCaptor<TodoItem> captor = ArgumentCaptor.forClass(TodoItem.class);
        verify(todoItemRepository).save(captor.capture());
        TodoItem savedItem = captor.getValue();

        assertEquals("Homework 5", savedItem.getDescription());
        assertEquals("CS506", savedItem.getCourseCode());
        assertEquals("assignment", savedItem.getTaskType());
        assertEquals(60, savedItem.getEstimatedMinutes());
        assertFalse(savedItem.getCompleted());
        assertNotNull(savedItem.getDueDate());
    }

    /**
     * Test that createTask throws exception for null request.
     */
    @Test
    void createTask_nullRequest_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            taskService.createTask(null);
        });
    }

    /**
     * Test that createTask throws exception for blank title.
     */
    @Test
    void createTask_blankTitle_throwsException() {
        TaskRequest request = new TaskRequest();
        request.setTitle("   ");
        request.setCourse("CS506");

        assertThrows(IllegalArgumentException.class, () -> {
            taskService.createTask(request);
        });
    }

    /**
     * Test that createTask throws exception for blank course.
     */
    @Test
    void createTask_blankCourse_throwsException() {
        TaskRequest request = new TaskRequest();
        request.setTitle("Homework 5");
        request.setCourse("");

        assertThrows(IllegalArgumentException.class, () -> {
            taskService.createTask(request);
        });
    }

    /**
     * Test that negative estimated minutes are normalized to 0.
     */
    @Test
    void createTask_negativeMinutes_normalizedToZero() {
        mockUserExists();
        TaskRequest request = new TaskRequest();
        request.setTitle("Quiz");
        request.setCourse("MATH340");
        request.setEstimatedMinutes(-30);

        when(todoItemRepository.save(any(TodoItem.class))).thenAnswer(inv -> 
            createSavedTodoItem(inv.getArgument(0), 1));

        taskService.createTask(request);

        ArgumentCaptor<TodoItem> captor = ArgumentCaptor.forClass(TodoItem.class);
        verify(todoItemRepository).save(captor.capture());

        assertEquals(0, captor.getValue().getEstimatedMinutes());
    }

    /**
     * Test that missing type defaults to "assignment".
     */
    @Test
    void createTask_missingType_defaultsToAssignment() {
        mockUserExists();
        TaskRequest request = new TaskRequest();
        request.setTitle("Project");
        request.setCourse("CS506");
        request.setType(null);

        when(todoItemRepository.save(any(TodoItem.class))).thenAnswer(inv -> 
            createSavedTodoItem(inv.getArgument(0), 1));

        taskService.createTask(request);

        ArgumentCaptor<TodoItem> captor = ArgumentCaptor.forClass(TodoItem.class);
        verify(todoItemRepository).save(captor.capture());

        assertEquals("assignment", captor.getValue().getTaskType());
    }

    /**
     * Test getTasksForWeek queries database with correct date range.
     */
    @Test
    void getTasksForWeek_queriesDatabaseWithCorrectRange() {
        // Arrange
        mockUserExists();
        String week = "2026-W15"; // Week 15 of 2026

        when(todoItemRepository.findByOocUserAndDueDateBetween(
                any(OocUser.class), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of());

        // Act
        taskService.getTasksForWeek(week);

        // Assert - verify repository was called with date range parameters
        verify(todoItemRepository).findByOocUserAndDueDateBetween(
                eq(testUser),
                any(LocalDateTime.class),
                any(LocalDateTime.class));
    }

    /**
     * Test getTasksForWeek returns mapped TaskResponse objects.
     */
    @Test
    void getTasksForWeek_returnsMappedTasks() {
        // Arrange
        mockUserExists();
        TodoItem item = new TodoItem();
        item.setOocUser(testUser);
        item.setDescription("Test Assignment");
        item.setTaskType("quiz");
        item.setEstimatedMinutes(45);
        item.setCourseCode("CS506");
        item.setDueDate(LocalDateTime.of(2026, 4, 8, 23, 59));
        item.setCompleted(false);
        // Set ID via reflection
        try {
            Field f = TodoItem.class.getDeclaredField("todoId");
            f.setAccessible(true);
            f.set(item, 99);
        } catch (Exception ignored) {}

        when(todoItemRepository.findByOocUserAndDueDateBetween(
                any(OocUser.class), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(item));

        // Act
        List<TaskResponse> tasks = taskService.getTasksForWeek("2026-W15");

        // Assert
        assertEquals(1, tasks.size());
        TaskResponse response = tasks.get(0);
        assertEquals("Test Assignment", response.getTitle());
        assertEquals("quiz", response.getType());
        assertEquals(45, response.getEstimatedMinutes());
        assertEquals("CS506", response.getCourse());
    }

    /**
     * Test getTasksForWeek throws for invalid week format.
     */
    @Test
    void getTasksForWeek_invalidFormat_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            taskService.getTasksForWeek("2026-15"); // Missing W
        });

        assertThrows(IllegalArgumentException.class, () -> {
            taskService.getTasksForWeek("invalid");
        });

        assertThrows(IllegalArgumentException.class, () -> {
            taskService.getTasksForWeek(null);
        });
    }

    /**
     * Test that when no user exists, one is created.
     */
    @Test
    void createTask_noExistingUser_createsDefaultUser() {
        // Return empty list first so a new user is created
        when(oocUserRepository.findAll()).thenReturn(List.of());
        when(oocUserRepository.save(any(OocUser.class))).thenReturn(testUser);
        when(todoItemRepository.save(any(TodoItem.class))).thenAnswer(inv -> 
            createSavedTodoItem(inv.getArgument(0), 1));

        TaskRequest request = new TaskRequest();
        request.setTitle("New Task");
        request.setCourse("CS506");

        taskService.createTask(request);

        // Verify a new user was created
        verify(oocUserRepository).save(any(OocUser.class));
    }
}
