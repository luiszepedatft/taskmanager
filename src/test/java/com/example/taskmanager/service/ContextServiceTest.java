package com.example.taskmanager.service;

import com.example.taskmanager.dto.ContextDTO;
import com.example.taskmanager.dto.CreateContextRequest;
import com.example.taskmanager.dto.UpdateContextRequest;
import com.example.taskmanager.mapper.ContextMapper;
import com.example.taskmanager.model.Context;
import com.example.taskmanager.model.Task;
import com.example.taskmanager.model.User;
import com.example.taskmanager.repository.ContextRepository;
import com.example.taskmanager.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ContextServiceTest {
    @Mock
    ContextRepository contextRepository;
    @Mock
    ContextMapper contextMapper;
    @Mock
    UserRepository userRepository;

    @InjectMocks
    private ContextService contextService;

    private Context context;
    private ContextDTO contextDTO;
    private User user;
    private Task task;

    private CreateContextRequest createContextRequest;
    private UpdateContextRequest updateContextRequest;

    @BeforeEach
    void setUp() {
        context = new Context();
        user = new User();
        task = new Task();

        context.setUser(user);
        context.setId(1L);
        context.setName("context");
        context.setDescription("description");
        context.setCreatedAt(LocalDateTime.now());
        context.setUpdatedAt(LocalDateTime.now());

        user.setUsername("username");
        user.setId(1L);

        task.setId(1L);
        task.setContext(context);
        task.setDescription("description");
        task.setUser(user);
        task.setTitle("title");

        contextDTO = new ContextDTO();
        contextDTO.setName("context");
        contextDTO.setDescription("description");
        contextDTO.setCreatedAt(LocalDateTime.now());
        contextDTO.setUpdatedAt(LocalDateTime.now());
        contextDTO.setId(1L);

        createContextRequest = new CreateContextRequest();
        createContextRequest.setName("context");
        createContextRequest.setDescription("description");

        updateContextRequest = new UpdateContextRequest();
        updateContextRequest.setName("context");
        updateContextRequest.setDescription("description");
    }

    @Nested
    @DisplayName("Create context")
    class CreateContext {

        @Test
        @DisplayName("should create a valid context")
        void shouldCreateValidContext() {
            when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
            when(contextMapper.toEntity(createContextRequest)).thenReturn(context);
            when(contextRepository.save(context)).thenReturn(context);
            when(contextMapper.toDTO(context)).thenReturn(contextDTO);

            ContextDTO result = contextService.createContext(createContextRequest, user.getUsername());

            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo(createContextRequest.getName());

            verify(contextRepository).save(context);
            verify(contextMapper).toDTO(context);
        }
    }

}
