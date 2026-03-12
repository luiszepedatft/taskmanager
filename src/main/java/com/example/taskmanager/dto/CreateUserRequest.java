package com.example.taskmanager.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateUserRequest {

    @NotBlank(message = "username is required")
    @Size(min = 3, max = 100, message = "username should be at least 3 characters long")
    private String username;

    @NotBlank(message = "email is required")
    @Email
    private String email;

    @NotBlank
    @Size(min = 6, message = "password should be at least 6 characters long")
    private String password;

}
