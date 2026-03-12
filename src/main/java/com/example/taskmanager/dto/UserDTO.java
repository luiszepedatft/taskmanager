package com.example.taskmanager.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {
    private Long id;
    private String username;
    private String email;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    // This might slow down user operations so not adviced
    // but this is how it would be done:
    // private List<TaskDTO> tasks;
    // private List<ContextDTO> contexts;
}
