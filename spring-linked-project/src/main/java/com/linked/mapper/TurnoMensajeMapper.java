package com.linked.mapper;

import com.linked.dto.TurnoMensajeDto;
import com.linked.model.entity.TurnoMensaje;
import org.mapstruct.*;

import java.util.List;

/** Convierte mensajes de turno a DTO, exponiendo solo el id y el username del autor. */
@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TurnoMensajeMapper {

    @Mapping(target = "usuarioEnviaId",
             expression = "java(m.getUsuarioEnvia() != null ? m.getUsuarioEnvia().getId() : null)")
    @Mapping(target = "usuarioEnviaUsername",
             expression = "java(m.getUsuarioEnvia() != null ? m.getUsuarioEnvia().getUsername() : null)")
    TurnoMensajeDto.Response toResponse(TurnoMensaje m);

    List<TurnoMensajeDto.Response> toResponseList(List<TurnoMensaje> list);
}
