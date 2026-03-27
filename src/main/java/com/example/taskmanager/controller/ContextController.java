package com.example.taskmanager.controller;

import com.example.taskmanager.dto.ContextDTO;
import com.example.taskmanager.dto.CreateContextRequest;
import com.example.taskmanager.dto.TaskDTO;
import com.example.taskmanager.dto.UpdateContextRequest;
import com.example.taskmanager.service.ContextService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/contexts")
@Tag(name = "Context controller")
public class ContextController {
    private final ContextService contextService;
    public ContextController(ContextService contextService) {
        this.contextService = contextService;
    }

    @Operation(summary = "create a context")
    @PostMapping()
    public ResponseEntity<ContextDTO> createContext(@RequestBody @Valid CreateContextRequest request) {
        String username = getCurrentUsername();
        ContextDTO contextDTO = contextService.createContext(request,username);
        return ResponseEntity.status(HttpStatus.CREATED).body(contextDTO);
    }

    @Operation(summary = "get all contexts for a user")
    @GetMapping()
    public ResponseEntity<List<ContextDTO>> getAllContexts() {
        String username = getCurrentUsername();
        List<ContextDTO> contextDTOs = contextService.getAllContexts(username);
        return ResponseEntity.ok().body(contextDTOs);
    }

    @Operation(summary = "get a context by id")
    @GetMapping("/{id}")
    public ResponseEntity<ContextDTO> getContext(@PathVariable Long id) {
        String username = getCurrentUsername();
        ContextDTO contextDTO = contextService.getContextById(id,username);
        return ResponseEntity.ok().body(contextDTO);
    }

    @Operation(summary = "update a context by id")
    @PatchMapping("/{id}")
    public ResponseEntity<ContextDTO> updateContext(@PathVariable Long id, @Valid @RequestBody UpdateContextRequest request) {
        String username = getCurrentUsername();
        ContextDTO contextDTO = contextService.updateContextById(id,request,username);
        return ResponseEntity.ok().body(contextDTO);
    }

    @Operation(summary = "delete a context by id")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteContext(@PathVariable Long id) {
        String username = getCurrentUsername();
        contextService.deleteContextById(id,username);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "gell all tasks belonging to a context")
    @GetMapping("/{id}/tasks")
    public ResponseEntity<List<TaskDTO>> getAllTasks(@PathVariable Long id) {
        String username = getCurrentUsername();
        List<TaskDTO> tasks = contextService.getTasksByContextId(id,username);
        return ResponseEntity.ok().body(tasks);
    }

    private String getCurrentUsername() {
        return Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getName();
    }
}
