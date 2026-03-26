package com.example.taskmanager.controller;

import com.example.taskmanager.dto.CreateTaskRequest;
import com.example.taskmanager.dto.TaskDTO;
import com.example.taskmanager.dto.UpdateTaskRequest;
import com.example.taskmanager.dto.UserDTO;
import com.example.taskmanager.exception.GlobalExceptionHandler;
import com.example.taskmanager.exception.UnauthorizedException;
import com.example.taskmanager.security.JwtUtil;
import com.example.taskmanager.service.ContextService;
import com.example.taskmanager.service.TaskService;
import com.example.taskmanager.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest(controllers = {TaskController.class, GlobalExceptionHandler.class})
public class TaskControllerTest {

    private final ObjectMapper objectMapper = createObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private TaskService taskService;

    @MockitoBean
    private ContextService contextService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private TaskDTO taskDTO;
    private UserDTO userDTO;
    private CreateTaskRequest createTaskRequest;
    private UpdateTaskRequest updateTaskRequest;

    private static ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }

    @BeforeEach
    void setUp() {
        taskDTO = new TaskDTO();
        taskDTO.setId(1L);
        taskDTO.setTitle("title");
        taskDTO.setDescription("description");
        taskDTO.setCreatedAt(LocalDateTime.now());
        taskDTO.setUpdatedAt(LocalDateTime.now());
        taskDTO.setDueDate(LocalDate.now());
        taskDTO.setContextId(42L);

        userDTO = new UserDTO();
        userDTO.setUsername("user");

        createTaskRequest = new CreateTaskRequest();
        createTaskRequest.setTitle("title");
        createTaskRequest.setDescription("description");
        createTaskRequest.setDueDate(LocalDate.now());
        createTaskRequest.setContextId(42L);

        updateTaskRequest = new UpdateTaskRequest();
        updateTaskRequest.setTitle("new title");
        updateTaskRequest.setDescription("new description");
        updateTaskRequest.setDone(true);
    }

    @Test
    @DisplayName("should delete a task")
    @WithMockUser
    void shouldDeleteTask() throws Exception {
        doNothing().when(taskService).deleteTaskById(any(Long.class), any(String.class));

        mockMvc.perform(delete("/api/tasks/{id}", taskDTO.getId()))
                .andExpect(status().isNoContent());

        verify(taskService).deleteTaskById(any(Long.class), any(String.class));


    }

    @Nested
    @DisplayName("POST /api/task")
    class PostTask {

        @Test
        @DisplayName("should create a task, return 201")
        @WithMockUser
        void shouldCreateTask() throws Exception {

            when(taskService.createTask(any(CreateTaskRequest.class), any(String.class)))
                    .thenReturn(taskDTO);

            mockMvc.perform(post("/api/tasks")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createTaskRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id", is(taskDTO.getId().intValue())))
                    .andExpect(jsonPath("$.title", is(taskDTO.getTitle())));

            verify(taskService).createTask(any(CreateTaskRequest.class), any(String.class));
        }
    }

    @Nested
    @DisplayName("GET /api/tasks")
    class GetTask {

        @Test
        @DisplayName("should return tasks for logged in user")
        @WithMockUser
        void shouldReturnTasksForLoggedInUser() throws Exception {

            List<TaskDTO> tasks = Arrays.asList(taskDTO,  taskDTO);
            when(taskService.getAllTasks(any(String.class)))
                    .thenReturn(tasks);

            mockMvc.perform(get("/api/tasks")
            .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(userDTO.getUsername())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.[0].id", is(taskDTO.getId().intValue())))
                    .andExpect(jsonPath("$.[0].title", is(taskDTO.getTitle())));
        }
    }

    @Nested
    @DisplayName("GET /api/tasks/{id}")
    class GetTaskById {

        @Test
        @DisplayName("should return task when it belongs to the user")
        @WithMockUser
        void shouldReturnTaskWhenItBelongsToTheUser() throws Exception {
            when(taskService.getTaskById(any(Long.class), any(String.class)))
                    .thenReturn(taskDTO);

            mockMvc.perform(get("/api/tasks/{id}", taskDTO.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(taskDTO.getId().intValue())))
                    .andExpect(jsonPath("$.title", is(taskDTO.getTitle())));
        }

        @Test
        @DisplayName("should return 401 when unauthorized user")
        @WithMockUser
        void shouldReturn401WhenUnauthorizedUser() throws Exception {
            when(taskService.getTaskById(any(Long.class), any(String.class)))
                    .thenThrow(new UnauthorizedException());

            mockMvc.perform(get("/api/tasks/{id}", taskDTO.getId()))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error", is("UNAUTHORIZED")));
        }

    }

}
