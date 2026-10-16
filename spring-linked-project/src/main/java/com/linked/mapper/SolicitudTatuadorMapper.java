package com.linked.mapper;

import com.linked.dto.SolicitudTatuadorDto;
import com.linked.model.entity.PerfilUsuario;
import com.linked.model.entity.SolicitudTatuador;
import org.mapstruct.Context;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.Map;

/** Convierte solicitudes de tatuador a DTO; {@code status} expone el estado del trámite, no la baja lógica. */
@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR, uses = {UsuarioMapper.class, FormatoDeFechas.class})
public interface SolicitudTatuadorMapper {

    /** @param perfiles perfiles de los solicitantes */
    @Mapping(target = "status", source = "solicitudStatus")
    SolicitudTatuadorDto.Response toResponse(SolicitudTatuador s, @Context Map<Long, PerfilUsuario> perfiles);

    List<SolicitudTatuadorDto.Response> toResponseList(List<SolicitudTatuador> list,
                                                       @Context Map<Long, PerfilUsuario> perfiles);
}
