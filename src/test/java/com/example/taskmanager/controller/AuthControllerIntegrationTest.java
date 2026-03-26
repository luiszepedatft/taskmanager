package com.example.taskmanager.controller;

import com.example.taskmanager.dto.CreateUserRequest;
import com.example.taskmanager.dto.LoginRequest;
import com.example.taskmanager.dto.UserDTO;
import com.example.taskmanager.exception.DuplicateResourceException;
import com.example.taskmanager.exception.GlobalExceptionHandler;
import com.example.taskmanager.exception.UnauthorizedException;
import com.example.taskmanager.security.JwtUtil;
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

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest(controllers = {AuthController.class, GlobalExceptionHandler.class})
class AuthControllerIntegrationTest {

    // Create ObjectMapper manually since @WebMvcTest doesn't auto-configure it in Spring Boot 4.x
    private final ObjectMapper objectMapper = createObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private UserDTO userDTO;
    private CreateUserRequest createUserRequest;
    private LoginRequest loginRequest;

    /**
     * Creates an ObjectMapper configured for Java 8 date/time types.
     * This is needed because @WebMvcTest doesn't auto-configure Jackson in Spring Boot 4.x.
     */
    private static ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }

    @BeforeEach
    void setUp() {
        userDTO = new UserDTO();
        userDTO.setUsername("user");
        userDTO.setId(1L);
        userDTO.setEmail("test@email.com");

        createUserRequest = new CreateUserRequest();
        createUserRequest.setUsername("user");
        createUserRequest.setPassword("password");
        createUserRequest.setEmail("test@email.com");

        loginRequest = new LoginRequest();
        loginRequest.setUsername("user");
        loginRequest.setPassword("password");
    }

    @Nested
    @DisplayName("POST /api/auth/register")
    class AuthControllerRegisterTest {

        @Test
        @DisplayName("Should register a user")
        @WithMockUser
        void shouldRegisterUser() throws Exception {

            when(userService.register(any(CreateUserRequest.class)))
                    .thenReturn(userDTO);

            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createUserRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.username", is(userDTO.getUsername())))
                    .andExpect(jsonPath("$.id", is(userDTO.getId().intValue())));

            verify(userService).register(any(CreateUserRequest.class));
        }

        @Test
        @DisplayName("should return 400 when name is blank")
        void shouldReturn400WhenNameIsBlank() throws Exception {
            createUserRequest.setUsername("");

            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createUserRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status", is(400)))
                    .andExpect(jsonPath("$.error", is("VALIDATION_ERROR")))
                    .andExpect(jsonPath("$.fieldErrors", hasSize(greaterThan(0))));

            verify(userService, never()).register(any());
        }

        @Test
        @DisplayName("should return 409 if user already exists")
        void shouldReturn409IfUserExists() throws Exception {
            when(userService.register(any(CreateUserRequest.class)))
                    .thenThrow(new DuplicateResourceException("username", "user", "User"));

            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createUserRequest)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.status", is(409)))
                    .andExpect(jsonPath("$.error", is("CONFLICT")));
        }


    }
    @Nested
    @DisplayName("POST /api/auth/login")
    class AuthControllerLoginTest {

        @Test
        @DisplayName("should return token on successful login")
        @WithMockUser
        void shouldReturnTokenOnSuccessfulLogin() throws Exception {
            when(userService.login(any(LoginRequest.class)))
                    .thenReturn("mocked-token");

            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token", is("mocked-token")));
        }

        @Test
        @DisplayName("Should return 401 on wrong credentials")
        @WithMockUser
        void shouldReturn401OnWrongCredentials() throws Exception {
            when(userService.login(any(LoginRequest.class)))
                    .thenThrow(new UnauthorizedException());

            mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error", is("UNAUTHORIZED")));
        }


    }

}

