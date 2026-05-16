package com.oiloncanvas.backend.repository;

import com.oiloncanvas.backend.entity.OocUser;
import com.oiloncanvas.backend.entity.TodoItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for TodoItemRepository.
 * Uses H2 in-memory database to test actual JPA operations.
 * 
 * Run with: ./gradlew test
 */
@DataJpaTest
class TodoItemRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TodoItemRepository todoItemRepository;

    private OocUser testUser;

    @BeforeEach
    void setUp() {
        // Create and persist a test user
        testUser = new OocUser();
        entityManager.persist(testUser);
        entityManager.flush();
    }

    /**
     * Test that saving a TodoItem persists it to the database.
     */
    @Test
    void save_persistsTodoItem() {
        // Arrange
        TodoItem item = new TodoItem();
        item.setOocUser(testUser);
        item.setDescription("Complete homework");
        item.setTaskType("assignment");
        item.setEstimatedMinutes(60);
        item.setCourseCode("CS506");
        item.setDueDate(LocalDateTime.of(2026, 4, 15, 23, 59));
        item.setCompleted(false);

        // Act
        TodoItem saved = todoItemRepository.save(item);
        entityManager.flush();

        // Assert
        assertNotNull(saved.getTodoId());
        
        // Verify it's actually in the database
        TodoItem found = entityManager.find(TodoItem.class, saved.getTodoId());
        assertNotNull(found);
        assertEquals("Complete homework", found.getDescription());
        assertEquals("CS506", found.getCourseCode());
    }

    /**
     * Test findByOocUser returns only that user's items.
     */
    @Test
    void findByOocUser_returnsOnlyUserItems() {
        // Arrange - create another user with their own todo
        OocUser otherUser = new OocUser();
        entityManager.persist(otherUser);

        TodoItem item1 = createTodoItem(testUser, "User 1 Task");
        TodoItem item2 = createTodoItem(testUser, "User 1 Task 2");
        TodoItem item3 = createTodoItem(otherUser, "Other User Task");

        entityManager.persist(item1);
        entityManager.persist(item2);
        entityManager.persist(item3);
        entityManager.flush();

        // Act
        List<TodoItem> userItems = todoItemRepository.findByOocUser(testUser);

        // Assert
        assertEquals(2, userItems.size());
        assertTrue(userItems.stream().allMatch(item -> 
            item.getDescription().startsWith("User 1")));
    }

    /**
     * Test findByOocUserAndDueDateBetween returns items within date range.
     */
    @Test
    void findByOocUserAndDueDateBetween_returnsItemsInRange() {
        // Arrange
        LocalDateTime april6 = LocalDateTime.of(2026, 4, 6, 12, 0);
        LocalDateTime april8 = LocalDateTime.of(2026, 4, 8, 23, 59);
        LocalDateTime april10 = LocalDateTime.of(2026, 4, 10, 12, 0);
        LocalDateTime april15 = LocalDateTime.of(2026, 4, 15, 12, 0);

        TodoItem inRange1 = createTodoItem(testUser, "Task in range 1");
        inRange1.setDueDate(april6);

        TodoItem inRange2 = createTodoItem(testUser, "Task in range 2");
        inRange2.setDueDate(april8);

        TodoItem outOfRange = createTodoItem(testUser, "Task out of range");
        outOfRange.setDueDate(april15);

        entityManager.persist(inRange1);
        entityManager.persist(inRange2);
        entityManager.persist(outOfRange);
        entityManager.flush();

        // Act - query for April 6-10
        List<TodoItem> items = todoItemRepository.findByOocUserAndDueDateBetween(
                testUser, april6, april10);

        // Assert
        assertEquals(2, items.size());
        assertTrue(items.stream().allMatch(item -> 
            item.getDescription().contains("in range")));
    }

    /**
     * Test that items with null dueDate are excluded from date range queries.
     */
    @Test
    void findByOocUserAndDueDateBetween_excludesNullDueDates() {
        // Arrange
        LocalDateTime april6 = LocalDateTime.of(2026, 4, 6, 0, 0);
        LocalDateTime april12 = LocalDateTime.of(2026, 4, 12, 23, 59);

        TodoItem withDueDate = createTodoItem(testUser, "Has due date");
        withDueDate.setDueDate(april6);

        TodoItem noDueDate = createTodoItem(testUser, "No due date");
        noDueDate.setDueDate(null);

        entityManager.persist(withDueDate);
        entityManager.persist(noDueDate);
        entityManager.flush();

        // Act
        List<TodoItem> items = todoItemRepository.findByOocUserAndDueDateBetween(
                testUser, april6, april12);

        // Assert
        assertEquals(1, items.size());
        assertEquals("Has due date", items.get(0).getDescription());
    }

    /**
     * Test that findById returns the correct item.
     */
    @Test
    void findById_returnsCorrectItem() {
        // Arrange
        TodoItem item = createTodoItem(testUser, "Specific task");
        entityManager.persist(item);
        entityManager.flush();

        // Act
        var found = todoItemRepository.findById(item.getTodoId());

        // Assert
        assertTrue(found.isPresent());
        assertEquals("Specific task", found.get().getDescription());
    }

    /**
     * Test that deleteById removes the item from database.
     */
    @Test
    void deleteById_removesItem() {
        // Arrange
        TodoItem item = createTodoItem(testUser, "To be deleted");
        entityManager.persist(item);
        entityManager.flush();
        Integer id = item.getTodoId();

        // Act
        todoItemRepository.deleteById(id);
        entityManager.flush();

        // Assert
        TodoItem found = entityManager.find(TodoItem.class, id);
        assertNull(found);
    }

    /**
     * Test that updating an item persists changes.
     */
    @Test
    void save_updatesExistingItem() {
        // Arrange
        TodoItem item = createTodoItem(testUser, "Original title");
        entityManager.persist(item);
        entityManager.flush();
        entityManager.clear(); // Clear cache to force reload

        // Act
        TodoItem toUpdate = todoItemRepository.findById(item.getTodoId()).orElseThrow();
        toUpdate.setDescription("Updated title");
        toUpdate.setCompleted(true);
        todoItemRepository.save(toUpdate);
        entityManager.flush();
        entityManager.clear();

        // Assert
        TodoItem updated = entityManager.find(TodoItem.class, item.getTodoId());
        assertEquals("Updated title", updated.getDescription());
        assertTrue(updated.getCompleted());
    }

    /**
     * Helper method to create a TodoItem with required fields.
     */
    private TodoItem createTodoItem(OocUser user, String description) {
        TodoItem item = new TodoItem();
        item.setOocUser(user);
        item.setDescription(description);
        item.setTaskType("assignment");
        item.setCompleted(false);
        return item;
    }
}
