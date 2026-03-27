package com.example.taskmanager.controller;

import com.example.taskmanager.dto.CreateTaskRequest;
import com.example.taskmanager.dto.TaskDTO;
import com.example.taskmanager.dto.UpdateTaskRequest;
import com.example.taskmanager.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/tasks")
@Tag(name = "Task Controller")
public class TaskController {
    private final TaskService taskService;
    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @Operation(summary = "create a task")
    @PostMapping()
    public ResponseEntity<TaskDTO> createTask(@Valid @RequestBody CreateTaskRequest request) {
        String username = getCurrentUsername();
        TaskDTO taskDTO = taskService.createTask(request, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(taskDTO);
    }

    @Operation(summary = "get all tasks for a user")
    @GetMapping()
    public ResponseEntity<List<TaskDTO>> getTasks() {
        String username = getCurrentUsername();
        List<TaskDTO> tasks = taskService.getAllTasks(username);
        return ResponseEntity.ok().body(tasks);
    }

    @Operation(summary = "get a task by Id for a user")
    @GetMapping("/{id}")
    public ResponseEntity<TaskDTO> getTask(@PathVariable Long id) {
        String username = getCurrentUsername();
        TaskDTO task = taskService.getTaskById(id,username);
        return ResponseEntity.ok().body(task);
    }

    @Operation(summary = "update a task by Id")
    @PatchMapping("/{id}")
    public ResponseEntity<TaskDTO> updateTask( @PathVariable Long id, @RequestBody @Valid UpdateTaskRequest request ){
        String username = getCurrentUsername();
        TaskDTO task = taskService.updateTaskById(id, request,username);
        return ResponseEntity.ok().body(task);
    }

    @Operation(summary = "delete a task by Id")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTask(@PathVariable Long id) {
        String username = getCurrentUsername();
        taskService.deleteTaskById(id,username);
        return ResponseEntity.noContent().build();
    }

    private String getCurrentUsername() {
        return Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getName();
    }

}
