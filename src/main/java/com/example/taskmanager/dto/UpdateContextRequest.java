package com.example.taskmanager.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateContextRequest {
    @Size(min = 3, max = 100, message = "title should be at least 3 characters long")
    private String name;

    @Size(max = 10000)
    private String description;
}
