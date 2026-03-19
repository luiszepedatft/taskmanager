package com.example.taskmanager.service;

import com.example.taskmanager.dto.CreateUserRequest;
import com.example.taskmanager.dto.LoginRequest;
import com.example.taskmanager.dto.UserDTO;
import com.example.taskmanager.exception.DuplicateResourceException;
import com.example.taskmanager.mapper.UserMapper;
import com.example.taskmanager.model.User;
import com.example.taskmanager.repository.UserRepository;
import com.example.taskmanager.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private UserService userService;

    private User user;
    private UserDTO userDTO;
    private CreateUserRequest createUserRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("username");
        user.setPassword("password");
        user.setEmail("email@test.com");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        userDTO = new UserDTO();
        userDTO.setUsername("username");
        userDTO.setEmail("email@test.com");

        createUserRequest = new CreateUserRequest();
        createUserRequest.setUsername("username");
        createUserRequest.setPassword("password");
        createUserRequest.setEmail("email@test.com");

        loginRequest = new LoginRequest();
        loginRequest.setUsername("username");
        loginRequest.setPassword("password");
    }

    @Nested
    @DisplayName("Register user test")
    class RegisterUserTest{

        @Test
        @DisplayName("should register a valid user")
        void shouldRegisterUser(){
            // arrange (mocking dependencies)
            when(userRepository.findByUsername("username")).thenReturn(Optional.empty()); // no duplicate
            when(userMapper.toEntity(createUserRequest)).thenReturn(user);
            when(passwordEncoder.encode("password")).thenReturn("hashedPassword");
            when(userRepository.save(any(User.class))).thenReturn(user);
            when(userMapper.toDTO(user)).thenReturn(userDTO);

            // Act
            UserDTO result = userService.register(createUserRequest);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getUsername()).isEqualTo(createUserRequest.getUsername());

            // Verify
            verify(userRepository).findByUsername(user.getUsername());
            verify(userMapper).toDTO(user);
        }

        @Test
        @DisplayName("should fail when registering with a duplicate username")
        void duplicated_User_ThrowsException(){
            // arrange (mocking dependencies)
            when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));

            // Act & Assert
            assertThatThrownBy(() -> userService.register(createUserRequest))
                    .isInstanceOf(DuplicateResourceException.class)
            .hasMessageContaining("username already exists");

            // Verify
            verify(userRepository, never()).save(any());
        }

    }

}
