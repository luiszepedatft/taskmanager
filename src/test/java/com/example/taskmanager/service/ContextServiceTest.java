package com.example.taskmanager.service;

import com.example.taskmanager.dto.ContextDTO;
import com.example.taskmanager.dto.CreateContextRequest;
import com.example.taskmanager.dto.UpdateContextRequest;
import com.example.taskmanager.exception.UnauthorizedException;
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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

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
    private User wrongUser;

    private CreateContextRequest createContextRequest;
    private UpdateContextRequest updateContextRequest;

    @BeforeEach
    void setUp() {
        context = new Context();
        user = new User();
        task = new Task();
        wrongUser = new User();

        context.setUser(user);
        context.setId(1L);
        context.setName("context");
        context.setDescription("description");
        context.setCreatedAt(LocalDateTime.now());
        context.setUpdatedAt(LocalDateTime.now());

        user.setUsername("username");
        user.setId(1L);

        wrongUser.setUsername("wrongUsername");
        wrongUser.setId(42L);

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

    @Nested
    @DisplayName("Update context")
    class UpdateContext {

        @Test
        @DisplayName("should update a valid context")
        void shouldUpdateValidContext() {
            when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
            when(contextRepository.findById(context.getId())).thenReturn(Optional.of(context));
            // since this method returns void we ask mockito to do nothing
            doNothing().when(contextMapper).updateEntityFromRequest(updateContextRequest, context);
            // this method returns a Context so we can ask mockito to stub this value
            when(contextRepository.save(context)).thenReturn(context);
            when(contextMapper.toDTO(context)).thenReturn(contextDTO);

            ContextDTO result = contextService.updateContextById(context.getId(),updateContextRequest, user.getUsername());

            assertThat(result).isNotNull();
            verify(contextMapper).updateEntityFromRequest(updateContextRequest, context);
            verify(contextRepository).save(context);
        }

    }

    @Nested
    @DisplayName("Delete context")
    class DeleteContext {

        @Test
        @DisplayName("should delete a valid context")
        void shouldDeleteContext() {
            when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
            when(contextRepository.findById(context.getId())).thenReturn(Optional.of(context));
            doNothing().when(contextRepository).delete(context);

            contextService.deleteContextById(context.getId(),user.getUsername());

            verify(contextRepository).delete(context);
        }
    }

    @Nested
    @DisplayName("Get context tests")
    class GetAllContextsForUser {

        @Test
        @DisplayName("Should get all contexts for a user")
        void shouldGetAllContextsForUser() {
            List<Context> contexts = Arrays.asList(context, context);
            List<ContextDTO> contextDTOs = Arrays.asList(contextDTO,  contextDTO);
            when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
            when(contextRepository.findAllByUserId(user.getId())).thenReturn(contexts);
            when(contextMapper.toDTO(contexts)).thenReturn(contextDTOs);

            List<ContextDTO> result = contextService.getAllContexts((user.getUsername()));

            assertThat(result).isNotNull();
            assertThat(result).hasSize(2);

            verify(contextRepository).findAllByUserId(user.getId());
        }

        @Test
        @DisplayName("Should return contextId when it belongs to a user")
        void shouldReturnContextIdForAuthorizedUser() {
            when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
            when(contextRepository.findById(context.getId())).thenReturn(Optional.of(context));
            when(contextMapper.toDTO(context)).thenReturn(contextDTO);

            ContextDTO result = contextService.getContextById(context.getId(),user.getUsername());

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(context.getId());

            verify(contextRepository).findById(context.getId());
            verify(contextMapper).toDTO(context);
        }

        @Test
        @DisplayName("should throw unauthorized exception when task doesn't belong to user")
        void shouldThrowExceptionWhenUnauthorizedUser() {
            when(userRepository.findByUsername(wrongUser.getUsername())).thenReturn(Optional.of(wrongUser));
            when(contextRepository.findById(context.getId())).thenReturn(Optional.of(context));

            assertThatThrownBy(() -> contextService.getContextById(context.getId(),wrongUser.getUsername()))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessageContaining("not authorized");

            verify(contextRepository).findById(context.getId());
            verify(userRepository).findByUsername(wrongUser.getUsername());
        }

    }

}
