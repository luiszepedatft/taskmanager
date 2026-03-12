package com.example.taskmanager.repository;

import com.example.taskmanager.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    // all tasks for a user
    List<Task> findAllByUserId(Long userId);

    // all tasks in a context
    List<Task> findAllByContextId(Long contextId);

    // all tasks in a context belonging to a specific user
    List<Task> findAllByContextIdAndUserId(Long contextId, Long userId);

}
