package com.linked.repository;

import com.linked.model.entity.SweUsrapl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/** Acceso a usuarios ({@link SweUsrapl}). */
public interface SweUsrRepository extends JpaRepository<SweUsrapl, Long> {

    Optional<SweUsrapl> findByUsername(String username);

    Optional<SweUsrapl> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    /** Usuarios activos con el rol Tatuador vigente. */
    @Query("SELECT u FROM SweUsrapl u " +
           "JOIN u.userRoles ur " +
           "JOIN ur.rolapl r " +
           "WHERE r.description = 'Tatuador' AND u.status = 'true' AND ur.status = 'true'")
    List<SweUsrapl> findAllTatuadores();

    /**
     * Igual que {@link #findAllTatuadores()} pero trayendo las especialidades en la misma
     * consulta, para poder mapearlas fuera de la transacción (búsqueda pública).
     * Se trae un solo bag ({@code especialidades}) para no caer en MultipleBagFetchException.
     */
    @Query("SELECT DISTINCT u FROM SweUsrapl u " +
           "JOIN u.userRoles ur " +
           "JOIN ur.rolapl r " +
           "LEFT JOIN FETCH u.especialidades " +
           "WHERE r.description = 'Tatuador' AND u.status = 'true' AND ur.status = 'true'")
    List<SweUsrapl> findAllTatuadoresConEspecialidades();

    /** Tatuadores activos que tienen asignada la especialidad indicada. */
    @Query("SELECT u FROM SweUsrapl u " +
           "JOIN u.userRoles ur " +
           "JOIN ur.rolapl r " +
           "WHERE r.description = 'Tatuador' " +
           "AND u.status = 'true' AND ur.status = 'true' " +
           "AND :especialidadId IN (SELECT e.id FROM u.especialidades e)")
    List<SweUsrapl> findTatuadoresByEspecialidad(@Param("especialidadId") Long especialidadId);

    // Consultas de Spring Security. Traen credenciales y roles en la misma consulta porque
    // getPassword() y getAuthorities() se leen después de cerrada la transacción.

    @Query("SELECT u FROM SweUsrapl u " +
           "LEFT JOIN FETCH u.usrauth " +
           "LEFT JOIN FETCH u.userRoles " +
           "WHERE u.email = :identifier AND u.status = 'true'")
    Optional<SweUsrapl> findByEmailForAuth(@Param("identifier") String identifier);

    @Query("SELECT u FROM SweUsrapl u " +
           "LEFT JOIN FETCH u.usrauth " +
           "LEFT JOIN FETCH u.userRoles " +
           "WHERE u.username = :identifier AND u.status = 'true'")
    Optional<SweUsrapl> findByUsernameForAuth(@Param("identifier") String identifier);
}
