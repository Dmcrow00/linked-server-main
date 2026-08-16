package com.linked.repository;

import com.linked.model.entity.SweUsrapl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SweUsrRepository extends JpaRepository<SweUsrapl, Long> {

    Optional<SweUsrapl> findByUsername(String username);

    Optional<SweUsrapl> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM SweUsrapl u " +
           "JOIN u.userRoles ur " +
           "JOIN ur.rolapl r " +
           "WHERE r.description = 'Tatuador' AND u.status = 'true' AND ur.status = 'true'")
    List<SweUsrapl> findAllTatuadores();

    @Query("SELECT u FROM SweUsrapl u " +
           "JOIN u.userRoles ur " +
           "JOIN ur.rolapl r " +
           "WHERE r.description = 'Tatuador' " +
           "AND u.status = 'true' AND ur.status = 'true' " +
           "AND :especialidadId IN (SELECT e.id FROM u.especialidades e)")
    List<SweUsrapl> findTatuadoresByEspecialidad(@Param("especialidadId") Long especialidadId);

    @Query("SELECT u FROM SweUsrapl u LEFT JOIN FETCH u.usrauth WHERE u.email = :identifier AND u.status = 'true'")
    Optional<SweUsrapl> findByEmailForAuth(@Param("identifier") String identifier);

    @Query("SELECT u FROM SweUsrapl u LEFT JOIN FETCH u.usrauth WHERE u.username = :identifier AND u.status = 'true'")
    Optional<SweUsrapl> findByUsernameForAuth(@Param("identifier") String identifier);
}
