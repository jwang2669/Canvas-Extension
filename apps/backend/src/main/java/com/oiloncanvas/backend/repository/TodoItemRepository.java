package com.oiloncanvas.backend.repository;

import com.oiloncanvas.backend.entity.TodoItem;
import com.oiloncanvas.backend.entity.OocUser;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


/**
 * TodoItem JPA repository. Defines custom query method to find
 * a user's todo items by the OocUser.
 */
@Repository
public interface TodoItemRepository extends JpaRepository<TodoItem, Integer> {
    // find todo items by ooc user
    List<TodoItem> findByOocUser(OocUser oocUser);

    // find todo items for a user within an inclusive due_date window
    List<TodoItem> findByOocUserAndDueDateBetween(
        OocUser oocUser,
        LocalDateTime start,
        LocalDateTime end);
    
    // save, findById, findAll, deleteById, etc. are inherited and will be used by service.
}
