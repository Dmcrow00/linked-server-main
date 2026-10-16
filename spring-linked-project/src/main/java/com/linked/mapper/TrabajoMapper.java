package com.linked.mapper;

import com.linked.dto.TrabajoDto;
import com.linked.model.entity.PerfilUsuario;
import com.linked.model.entity.Trabajo;
import com.linked.model.entity.TrabajoFoto;
import org.mapstruct.Context;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Map;
import java.util.Set;

/** Convierte trabajos a DTO con la vista pública del tatuador. */
@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR, uses = {UsuarioMapper.class, FormatoDeFechas.class})
public interface TrabajoMapper {

    /**
     * @param perfiles  perfiles de los tatuadores (de ahí sale el estudio)
     * @param meGustan  ids de los trabajos que le gustan a quien consulta; vacío sin sesión
     */
    @Mapping(target = "likesCount", expression = "java(t.getFavoritos().size())")
    @Mapping(target = "likedByMe", expression = "java(meGustan.contains(t.getId()))")
    TrabajoDto.Response toResponse(Trabajo t, @Context Map<Long, PerfilUsuario> perfiles, @Context Set<Long> meGustan);

    default String ruta(TrabajoFoto foto) {
        return foto.getImagePath();
    }
}
