package com.example.taskmanager.service;

import com.example.taskmanager.dto.CreateUserRequest;
import com.example.taskmanager.dto.LoginRequest;
import com.example.taskmanager.dto.UserDTO;
import com.example.taskmanager.exception.DuplicateResourceException;
import com.example.taskmanager.exception.ResourceNotFoundException;
import com.example.taskmanager.exception.UnauthorizedException;
import com.example.taskmanager.mapper.UserMapper;
import com.example.taskmanager.model.User;
import com.example.taskmanager.repository.UserRepository;
import com.example.taskmanager.security.JwtUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    // Create a new user
    public UserDTO register(CreateUserRequest request){
        // Check for duplicate username
        if (userRepository.findByUsername(request.getUsername()).isPresent()){
            throw new DuplicateResourceException("username", request.getUsername(), "User");
        }

        User user = userMapper.toEntity(request);

        // hash password
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        User savedUser = userRepository.save(user);
        return userMapper.toDTO(savedUser);
    }

    public String login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", request.getUsername()));

        // Check if the password matches the hashed one in the database
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException();
        }

        return jwtUtil.generateToken(user.getUsername());
    }

}

