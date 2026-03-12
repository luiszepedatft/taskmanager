package com.example.taskmanager.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskDTO {
    private Long id;
    private String title;
    private String description;
    private LocalDate dueDate;
    private LocalDate completionDate;
    private Boolean done;
    private Boolean archived;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    // foreign keys
    private Long contextId;
}
