package com.example.taskmanager.mapper;

import com.example.taskmanager.dto.ContextDTO;
import com.example.taskmanager.dto.CreateContextRequest;
import com.example.taskmanager.dto.UpdateContextRequest;
import com.example.taskmanager.model.Context;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ContextMapper {
    ContextDTO toDTO(Context context);

    List<ContextDTO> toDTO(List<Context> contexts);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "tasks", ignore = true)
    Context toEntity(CreateContextRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromRequest(UpdateContextRequest request, @MappingTarget Context context);
}
