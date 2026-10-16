package com.linked.mapper;

import com.linked.dto.TurnoSesionDto;
import com.linked.model.entity.ImagenRef;
import com.linked.model.entity.PerfilUsuario;
import com.linked.model.entity.TurnoSesion;
import org.mapstruct.Context;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Map;

/**
 * Convierte turnos a su DTO de respuesta. La expresión de {@code tatuadores} usa
 * {@code usuarioMapper}, el campo que MapStruct genera para el mapper declarado en {@code uses}.
 */
@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR, uses = {UsuarioMapper.class, TurnoMensajeMapper.class, FormatoDeFechas.class})
public interface TurnoSesionMapper {

    /** @param perfiles perfiles del cliente y de los tatuadores del turno */
    @Mapping(target = "tatuadores",
             expression = "java(ts.getTurnoTatuadores().stream().map(tt -> usuarioMapper.toResponse(tt.getTatuador(), perfiles)).toList())")
    TurnoSesionDto.Response toResponse(TurnoSesion ts, @Context Map<Long, PerfilUsuario> perfiles);

    default String ruta(ImagenRef imagen) {
        return imagen.getImagePath();
    }
}
