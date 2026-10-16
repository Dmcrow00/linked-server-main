package com.linked.mapper;

import com.dmc00.core.swe.dto.SweRolDto;
import com.dmc00.core.swe.model.entity.SweRolapl;
import com.dmc00.core.swe.model.entity.SweUsrapl;
import com.linked.dto.UserDto;
import com.linked.model.entity.PerfilUsuario;
import org.mapstruct.Context;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.Map;

/**
 * Usuarios a DTO: la cuenta (core-swe) más su perfil de linked. El perfil puede faltar (usuario
 * creado por fuera de la app): esos campos salen en {@code null}. Fechas en {@code yyyy-MM-dd}.
 */
@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR, uses = {EspecialidadMapper.class, FormatoDeFechas.class})
public interface UsuarioMapper {

    @Mapping(target = "id", source = "usuario.id")
    @Mapping(target = "status", source = "usuario.status")
    @Mapping(target = "birthDate", source = "usuario.birthDate")
    @Mapping(target = "fechaInicioActividad", source = "perfil.fechaInicioActividad")
    @Mapping(target = "roles", source = "usuario.rolesVigentes")
    @Mapping(target = "especialidades", source = "perfil.especialidades")
    UserDto.Response toResponse(SweUsrapl usuario, PerfilUsuario perfil);

    /** Para listas: los perfiles se buscan una sola vez ({@code PerfilUsuarioRepository#porUsuario}). */
    default UserDto.Response toResponse(SweUsrapl usuario, @Context Map<Long, PerfilUsuario> perfiles) {
        return usuario != null ? toResponse(usuario, perfiles.get(usuario.getId())) : null;
    }

    /** Vista reducida para respuestas sin sesión; ver {@link UserDto.Publico}. */
    @Mapping(target = "id", source = "usuario.id")
    UserDto.Publico toPublico(SweUsrapl usuario, PerfilUsuario perfil);

    default UserDto.Publico toPublico(SweUsrapl usuario, @Context Map<Long, PerfilUsuario> perfiles) {
        return usuario != null ? toPublico(usuario, perfiles.get(usuario.getId())) : null;
    }

    List<SweRolDto.Response> toRoles(List<SweRolapl> roles);
}
