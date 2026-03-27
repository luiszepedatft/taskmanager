package com.example.taskmanager.service;

import com.example.taskmanager.dto.ContextDTO;
import com.example.taskmanager.dto.CreateContextRequest;
import com.example.taskmanager.dto.TaskDTO;
import com.example.taskmanager.dto.UpdateContextRequest;
import com.example.taskmanager.exception.ResourceNotFoundException;
import com.example.taskmanager.exception.UnauthorizedException;
import com.example.taskmanager.mapper.ContextMapper;
import com.example.taskmanager.mapper.TaskMapper;
import com.example.taskmanager.model.Context;
import com.example.taskmanager.model.Task;
import com.example.taskmanager.model.User;
import com.example.taskmanager.repository.ContextRepository;
import com.example.taskmanager.repository.TaskRepository;
import com.example.taskmanager.repository.UserRepository;
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
public class ContextService {
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final ContextMapper contextMapper;
    private final ContextRepository contextRepository;
    private final TaskMapper taskMapper;

    public ContextDTO createContext(CreateContextRequest request, String username) {
        Context newContext = contextMapper.toEntity(request);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User",  "username", username));
        newContext.setUser(user);
        Context savedContext = contextRepository.save(newContext);
        return contextMapper.toDTO(savedContext);
    }

    public List<ContextDTO> getAllContexts(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User",  "username", username));
        List<Context> contexts = contextRepository.findAllByUserId(user.getId());
        return contextMapper.toDTO(contexts);
    }

    @Cacheable(value = "contexts", key = "#contextId")
    public ContextDTO getContextById(Long contextId, String username) {
        Context context = verifyOwnership(contextId, username);
        return contextMapper.toDTO(context);
    }

    @CacheEvict(value = "contexts", key = "#contextId")
    public ContextDTO updateContextById(Long contextId, UpdateContextRequest request, String username) {
        Context context = verifyOwnership(contextId, username);
        contextMapper.updateEntityFromRequest(request, context);
        return contextMapper.toDTO(contextRepository.save(context));
    }

    @CacheEvict(value = "contexts", key = "#contextId")
    public void deleteContextById(Long contextId, String username) {
        Context context = verifyOwnership(contextId, username);
        contextRepository.delete(context);
    }

    public List<TaskDTO> getTasksByContextId(Long contextId, String username) {
       Context context = verifyOwnership(contextId, username);
       List<Task> tasks = taskRepository.findAllByContextId(context.getId());
       return taskMapper.toDTOs(tasks);
    }

    private Context verifyOwnership(Long contextId, String username){
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User",  "username", username));
        Context context = contextRepository.findById(contextId)
                .orElseThrow(() -> new ResourceNotFoundException("Context",  "id", contextId));
        if (!user.getId().equals(context.getUser().getId())) {
            throw new UnauthorizedException();
        }
        return context;
    }

}
