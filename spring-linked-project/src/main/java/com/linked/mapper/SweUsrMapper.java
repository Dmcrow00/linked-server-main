package com.linked.mapper;

import com.linked.dto.EspecialidadDto;
import com.linked.dto.SweRolDto;
import com.linked.dto.UserDto;
import com.linked.model.entity.Especialidad;
import com.linked.model.entity.SweRolapl;
import com.linked.model.entity.SweUsrapl;
import org.mapstruct.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface SweUsrMapper {

    @Mapping(target = "birthDate", expression = "java(formatDate(u.getBirthDate()))")
    @Mapping(target = "fechaInicioActividad", expression = "java(formatDate(u.getFechaInicioActividad()))")
    @Mapping(target = "roles", expression = "java(mapRoles(u))")
    @Mapping(target = "especialidades", expression = "java(mapEspecialidades(u))")
    UserDto.Response toResponse(SweUsrapl u);

    List<UserDto.Response> toResponseList(List<SweUsrapl> users);

    default String formatDate(LocalDate date) {
        return date != null ? date.format(DateTimeFormatter.ISO_LOCAL_DATE) : null;
    }

    default List<SweRolDto.Response> mapRoles(SweUsrapl u) {
        if (u.getUserRoles() == null) return List.of();
        return u.getUserRoles().stream()
                .filter(ur -> "true".equals(ur.getStatus()))
                .map(ur -> new SweRolDto.Response(
                        ur.getRolapl().getId(),
                        ur.getRolapl().getDescription(),
                        ur.getRolapl().getStatus()))
                .toList();
    }

    default List<EspecialidadDto.Response> mapEspecialidades(SweUsrapl u) {
        if (u.getEspecialidades() == null) return List.of();
        return u.getEspecialidades().stream()
                .map(e -> new EspecialidadDto.Response(e.getId(), e.getNombre(), e.getDescripcion(), e.getStatus()))
                .toList();
    }
}
