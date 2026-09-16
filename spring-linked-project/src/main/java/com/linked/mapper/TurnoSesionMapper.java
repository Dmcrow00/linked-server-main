package com.linked.mapper;

import com.linked.dto.TurnoSesionDto;
import com.linked.model.entity.TurnoSesion;
import org.mapstruct.*;

import java.util.List;

/**
 * Convierte turnos a su DTO de respuesta. Las fechas se exponen en ISO-8601 local.
 * <p>
 * Las expresiones usan {@code sweUsrMapper}, que es el campo que MapStruct genera en la
 * implementación para el mapper declarado en {@code uses}.
 */
@Mapper(componentModel = "spring",
        uses = {SweUsrMapper.class, TurnoMensajeMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TurnoSesionMapper {

    @Mapping(target = "cliente", source = "cliente")
    @Mapping(target = "tatuadores",
             expression = "java(ts.getTurnoTatuadores().stream().map(tt -> sweUsrMapper.toResponse(tt.getTatuador())).toList())")
    @Mapping(target = "imagenesRef",
             expression = "java(ts.getImagenesRef().stream().map(ir -> ir.getImagePath()).toList())")
    @Mapping(target = "mensajes", source = "mensajes")
    @Mapping(target = "creationTimestamp",
             expression = "java(ts.getCreationTimestamp() != null ? ts.getCreationTimestamp().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null)")
    @Mapping(target = "fechaHoraInicio",
             expression = "java(ts.getFechaHoraInicio() != null ? ts.getFechaHoraInicio().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null)")
    @Mapping(target = "fechaHoraFin",
             expression = "java(ts.getFechaHoraFin() != null ? ts.getFechaHoraFin().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null)")
    TurnoSesionDto.Response toResponse(TurnoSesion ts);

    List<TurnoSesionDto.Response> toResponseList(List<TurnoSesion> list);
}
