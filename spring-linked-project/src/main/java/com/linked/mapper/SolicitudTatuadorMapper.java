package com.linked.mapper;

import com.linked.dto.SolicitudTatuadorDto;
import com.linked.model.entity.SolicitudTatuador;
import org.mapstruct.*;

import java.util.List;

/** Convierte solicitudes de tatuador a DTO; {@code status} expone el estado del trámite, no la baja lógica. */
@Mapper(componentModel = "spring",
        uses = {SweUsrMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface SolicitudTatuadorMapper {

    @Mapping(target = "status", expression = "java(s.getSolicitudStatus() != null ? s.getSolicitudStatus().name() : null)")
    @Mapping(target = "creationTimestamp",
             expression = "java(s.getCreationTimestamp() != null ? s.getCreationTimestamp().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null)")
    SolicitudTatuadorDto.Response toResponse(SolicitudTatuador s);

    List<SolicitudTatuadorDto.Response> toResponseList(List<SolicitudTatuador> list);
}
