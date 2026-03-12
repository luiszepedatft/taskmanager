package com.example.taskmanager.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTaskRequest {

    @Size(min = 3, max = 500, message = "title should be at least 3 characters long")
    private String title;
    @Size(max = 10000)
    private String description;
    private LocalDate dueDate;
    private LocalDate completionDate;
    private Boolean done;
    private Boolean archived;
}
