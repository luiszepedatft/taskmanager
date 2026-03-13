package com.example.taskmanager.mapper;

import com.example.taskmanager.dto.CreateTaskRequest;
import com.example.taskmanager.dto.TaskDTO;
import com.example.taskmanager.dto.UpdateTaskRequest;
import com.example.taskmanager.model.Task;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TaskMapper {
    @Mapping(source = "context.id", target = "contextId")
    TaskDTO toDTO(Task task);

    List<TaskDTO> toDTOs(List<Task> tasks);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "context", ignore = true)
    Task toEntity(CreateTaskRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    // Handle context update on the service layer
    @Mapping(target = "context",  ignore = true)
    void updateEntityFromRequest(UpdateTaskRequest request, @MappingTarget Task task);
}
