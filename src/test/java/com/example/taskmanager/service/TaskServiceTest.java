package com.example.taskmanager.service;

import com.example.taskmanager.dto.CreateTaskRequest;
import com.example.taskmanager.dto.TaskDTO;
import com.example.taskmanager.dto.UpdateTaskRequest;
import com.example.taskmanager.exception.UnauthorizedException;
import com.example.taskmanager.mapper.TaskMapper;
import com.example.taskmanager.model.Context;
import com.example.taskmanager.model.Task;
import com.example.taskmanager.model.User;
import com.example.taskmanager.repository.ContextRepository;
import com.example.taskmanager.repository.TaskRepository;
import com.example.taskmanager.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ContextRepository contextRepository;
    @Mock
    private TaskMapper taskMapper;

    @InjectMocks
    private TaskService taskService;

    private Task task;
    private User user;
    private User wrongUser;
    private Context context;
    private TaskDTO taskDTO;

    private CreateTaskRequest createTaskRequest;
    private UpdateTaskRequest updateTaskRequest;

    @BeforeEach
    public void setUp() {
        task = new Task();
        user = new User();
        context = new Context();
        wrongUser = new User();

        context.setUser(user);
        context.setId(42L);
        context.setName("context");

        user.setUsername("username");
        user.setId(1L);

        wrongUser.setUsername("wrongUsername");
        wrongUser.setId(2L);

        task.setUser(user);
        task.setId(1L);
        task.setTitle("title");
        task.setDescription("description");
        task.setArchived(false);
        task.setDone(false);
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());

        taskDTO = new TaskDTO();
        taskDTO.setTitle("title");
        taskDTO.setDescription("description");
        taskDTO.setArchived(false);
        taskDTO.setDone(false);
        taskDTO.setCreatedAt(LocalDateTime.now());
        taskDTO.setUpdatedAt(LocalDateTime.now());
        taskDTO.setId(1L);

        createTaskRequest = new CreateTaskRequest();
        createTaskRequest.setTitle("title");
        createTaskRequest.setDescription("description");
        createTaskRequest.setDueDate(LocalDate.now());

        updateTaskRequest = new UpdateTaskRequest();
        updateTaskRequest.setTitle("updated title");
        updateTaskRequest.setDescription("updated description");
        updateTaskRequest.setDueDate(LocalDate.now());
        updateTaskRequest.setArchived(false);
        updateTaskRequest.setDone(true);
        updateTaskRequest.setContextId(42L);

    }

    @Nested
    @DisplayName("Create task test")
    class CreateTaskTest {

        @Test
        @DisplayName("should create a valid task")
        public void shouldCreateTask() {
            // arrange
            when(taskMapper.toEntity(createTaskRequest)).thenReturn(task);
            when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
            when(taskRepository.save(task)).thenReturn(task);
            when(taskMapper.toDTO(task)).thenReturn(taskDTO);

            // act
            TaskDTO result = taskService.createTask(createTaskRequest, user.getUsername());

            // assert
            assertThat(result).isNotNull();
            assertThat(result.getTitle()).isEqualTo(createTaskRequest.getTitle());

            //verify(taskRepository).findById(task.getId());
            verify(taskRepository).save(task);
            verify(taskMapper).toDTO(task);
        }
    }

    @Nested
    @DisplayName("Get task tests")
    class GetTaskTests {

        @Test
        @DisplayName("should return all tasks for a user")
        public void shouldReturnAllTasksForUser() {
            // arrange
            List<Task> tasks = Arrays.asList(task, task);
            List<TaskDTO> taskDTOs = Arrays.asList(taskDTO, taskDTO);
            when(taskRepository.findAllByUserId(user.getId())).thenReturn(tasks);
            when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
            when(taskMapper.toDTOs(tasks)).thenReturn(taskDTOs);

            // act
            List<TaskDTO> result = taskService.getAllTasks(user.getUsername());

            // assert
            assertThat(result).isNotNull();
            assertThat(result).hasSize(2);

            // verify
            verify(taskRepository).findAllByUserId(user.getId());
        }

        @Test
        @DisplayName("should return task when it belongs to a user")
        public void shouldReturnTaskWhenBelongsToUser() {
            // arrange
            when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
            when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
            when(taskMapper.toDTO(task)).thenReturn(taskDTO);

            // act
            TaskDTO result = taskService.getTaskById(task.getId(), user.getUsername());

            // assert
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(task.getId());

            // verify
            verify(taskRepository).findById(task.getId());
            verify(taskMapper).toDTO(task);
        }

        @Test
        @DisplayName("should throw unauthorized exception when task doesn't belong to user")
        public void shouldThrowExceptionWhenUnauthorized() {
            // arrange
            when(userRepository.findByUsername(wrongUser.getUsername())).thenReturn(Optional.of(wrongUser));
            when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));

            // act & assert
            assertThatThrownBy(() -> taskService.getTaskById(task.getId(), wrongUser.getUsername()))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessageContaining("not authorized");

            // verify
            verify(taskRepository).findById(task.getId());
            verify(userRepository).findByUsername(wrongUser.getUsername());
        }

    }

    @Nested
    @DisplayName("Delete task")
    class deleteTaskTests{

        @Test
        @DisplayName("should delete task")
        public void shouldDeleteTask() {
            when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
            when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
            doNothing().when(taskRepository).delete(task);

            taskService.deleteTaskById(task.getId(), user.getUsername());

            verify(taskRepository).delete(task);
        }
    }

    @Nested
    @DisplayName("Update task")
    class updateTaskTests{
        @Test
        @DisplayName("should update task")
        void shouldUpdateTask() {
            when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
            when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
            doNothing().when(taskMapper).updateEntityFromRequest(updateTaskRequest, task);
            when(contextRepository.findById(context.getId())).thenReturn(Optional.of(context));
            when(taskRepository.save(task)).thenReturn(task);
            when(taskMapper.toDTO(task)).thenReturn(taskDTO);

            TaskDTO result = taskService.updateTaskById(task.getId(), updateTaskRequest,user.getUsername());

            assertThat(result).isNotNull();
            verify(taskMapper).updateEntityFromRequest(updateTaskRequest, task);
            verify(taskRepository).save(task);
        }
    }

}