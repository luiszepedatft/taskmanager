package com.example.taskmanager.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateContextRequest {
    @NotBlank(message = "name is required")
    @Size(min = 3, max = 100, message = "name should be at least 3 characters long")
    private String name;

    @Size(max = 10000)
    private String description;
}
