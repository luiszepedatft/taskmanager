package com.example.taskmanager.service;

import com.example.taskmanager.dto.CreateTaskRequest;
import com.example.taskmanager.dto.TaskDTO;
import com.example.taskmanager.dto.UpdateTaskRequest;
import com.example.taskmanager.exception.ResourceNotFoundException;
import com.example.taskmanager.exception.UnauthorizedException;
import com.example.taskmanager.mapper.TaskMapper;
import com.example.taskmanager.model.Context;
import com.example.taskmanager.model.Task;
import com.example.taskmanager.model.User;
import com.example.taskmanager.repository.ContextRepository;
import com.example.taskmanager.repository.TaskRepository;
import com.example.taskmanager.repository.UserRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataAccessException;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Retryable(
        includes = { DataAccessException.class},
        maxRetries = 3,
        delayString = "500ms",
        multiplier = 2,
        maxDelay = 5000
)
public class TaskService {
    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;
    private final UserRepository userRepository;
    private final ContextRepository contextRepository;

    public TaskDTO createTask(CreateTaskRequest request, String username) {
        Task newTask = taskMapper.toEntity(request);
        User user = userRepository.findByUsername(username)
                        .orElseThrow(() -> new ResourceNotFoundException("User",  "username", username));
        newTask.setUser(user);

        if (request.getContextId() != null) {
            Context context = contextRepository.findById(request.getContextId())
                    .orElseThrow(() -> new ResourceNotFoundException("Context",  "contextId", request.getContextId()));
            if (context.getUser().getId().equals(user.getId())) {
                newTask.setContext(context);
            } else {
                throw new UnauthorizedException();
            }
        }
        Task savedTask = taskRepository.save(newTask);
        return taskMapper.toDTO(savedTask);
    }

    public List<TaskDTO> getAllTasks(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User",  "username", username));
        List<Task> tasks = taskRepository.findAllByUserId(user.getId());
        return taskMapper.toDTOs(tasks);

    }

    @CircuitBreaker(name = "taskService", fallbackMethod = "getTaskFallback")
    @Cacheable(value = "tasks", key = "#taskId")
    public TaskDTO getTaskById(Long taskId, String username) {
        Task task = verifyOwnership(taskId,username);
        return taskMapper.toDTO(task);
    }

    private TaskDTO getTaskFallback(Long taskId, String username, Throwable ex) {
        throw new RuntimeException("Task is unavailable, try again later", ex);
    }


    @CacheEvict(value = "tasks", key = "#taskId")
    public TaskDTO updateTaskById(Long taskId, UpdateTaskRequest request, String username) {
        Task task = verifyOwnership(taskId,username);
            if (request.getContextId() != null) {
                Context context = contextRepository.findById(request.getContextId())
                        .orElseThrow(() -> new ResourceNotFoundException("Context", "id", request.getContextId()));
                if (context.getUser().getId().equals(task.getUser().getId())) {
                    task.setContext(context);
                } else {
                    throw new UnauthorizedException();
                }
            }
            taskMapper.updateEntityFromRequest(request, task);
            Task savedTask = taskRepository.save(task);
            return taskMapper.toDTO(savedTask);
    }

    @CacheEvict(value = "tasks", key = "#taskId")
    public void deleteTaskById(Long taskId, String username) {
        Task task = verifyOwnership(taskId,username);
        taskRepository.delete(task);
    }

    // check if task belongs to a user, and return the Task if it does
    private Task verifyOwnership(Long taskId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User",  "username", username));
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task",  "id", taskId));
        if (!task.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException();
        }
        return task;
    }

}
