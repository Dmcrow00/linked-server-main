package com.linked.mapper;

import com.linked.dto.TurnoMensajeDto;
import com.linked.model.entity.TurnoMensaje;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/** Convierte mensajes de turno a DTO, exponiendo solo el id y el username del autor. */
@Mapper(componentModel = "spring", uses = FormatoDeFechas.class)
public interface TurnoMensajeMapper {

    @Mapping(target = "usuarioEnviaId", source = "usuarioEnvia.id")
    @Mapping(target = "usuarioEnviaUsername", source = "usuarioEnvia.username")
    TurnoMensajeDto.Response toResponse(TurnoMensaje m);

    List<TurnoMensajeDto.Response> toResponseList(List<TurnoMensaje> list);
}
