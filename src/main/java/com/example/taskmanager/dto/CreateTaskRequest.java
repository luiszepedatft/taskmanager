package com.example.taskmanager.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTaskRequest {
    @NotBlank(message = "title is required")
    @Size(min = 3, max = 100, message = "title should be at least 3 characters")
    private String title;

    private String description;

    private LocalDate dueDate;

    private Long contextId;
}
