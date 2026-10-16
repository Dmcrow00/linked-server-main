package com.linked.repository;

import com.linked.model.entity.PerfilUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Acceso a perfiles de usuario ({@link PerfilUsuario}). */
public interface PerfilUsuarioRepository extends JpaRepository<PerfilUsuario, Long> {

    /**
     * Perfiles de los usuarios activos con el rol Tatuador vigente, con cuenta y especialidades
     * en la misma consulta: la búsqueda pública por mapa los recorre todos.
     */
    @Query("""
            SELECT DISTINCT p FROM PerfilUsuario p
            JOIN FETCH p.usuario u
            LEFT JOIN FETCH p.especialidades
            JOIN u.userRoles ur
            JOIN ur.rolapl r
            WHERE r.description = 'Tatuador' AND u.status = 'true' AND ur.status = 'true'
            """)
    List<PerfilUsuario> findAllTatuadoresConEspecialidades();

    /** Los perfiles de esos usuarios, por id. Un usuario sin perfil no aparece en el mapa. */
    default Map<Long, PerfilUsuario> porUsuario(Collection<Long> idsDeUsuarios) {
        return findAllById(idsDeUsuarios).stream()
                .collect(Collectors.toMap(PerfilUsuario::getId, Function.identity()));
    }
}
