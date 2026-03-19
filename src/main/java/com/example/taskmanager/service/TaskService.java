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
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
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
            newTask.setContext(context);
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

    public TaskDTO getTaskById(Long taskId, String username) {
        Task task = verifyOwnership(taskId,username);
        return taskMapper.toDTO(task);
    }

    public TaskDTO updateTaskById(Long taskId, UpdateTaskRequest request, String username) {
        Task task = verifyOwnership(taskId,username);
            if (request.getContextId() != null) {
                Context context = contextRepository.findById(request.getContextId())
                        .orElseThrow(() -> new ResourceNotFoundException("Context", "id", request.getContextId()));
                task.setContext(context);
            }
            taskMapper.updateEntityFromRequest(request, task);
            return taskMapper.toDTO(task);
    }

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
