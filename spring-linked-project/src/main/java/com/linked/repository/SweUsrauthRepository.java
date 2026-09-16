package com.linked.repository;

import com.linked.model.entity.SweUsrauth;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/** Acceso a credenciales de usuario ({@link SweUsrauth}). */
public interface SweUsrauthRepository extends JpaRepository<SweUsrauth, Long> {

    Optional<SweUsrauth> findByUsrapl_Id(Long usraplId);

    /** Credenciales asociadas a un token de recuperación de contraseña. */
    Optional<SweUsrauth> findByResetPasswordToken(String token);

    void deleteByUsrapl_Id(Long usraplId);
}
