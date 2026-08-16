package com.linked.mapper;

import com.linked.dto.EspecialidadDto;
import com.linked.model.entity.Especialidad;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface EspecialidadMapper {

    EspecialidadDto.Response toResponse(Especialidad e);

    List<EspecialidadDto.Response> toResponseList(List<Especialidad> list);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "creationUser", ignore = true)
    @Mapping(target = "creationTimestamp", ignore = true)
    @Mapping(target = "modificationUser", ignore = true)
    @Mapping(target = "modificationTimestamp", ignore = true)
    @Mapping(target = "status", ignore = true)
    Especialidad toEntity(EspecialidadDto.CreateRequest req);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "creationUser", ignore = true)
    @Mapping(target = "creationTimestamp", ignore = true)
    @Mapping(target = "modificationUser", ignore = true)
    @Mapping(target = "modificationTimestamp", ignore = true)
    @Mapping(target = "status", ignore = true)
    void updateEntity(EspecialidadDto.UpdateRequest req, @MappingTarget Especialidad entity);
}
