package com.linked.model.entity;

import com.core.model.entity.SweAuditableEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Credenciales de acceso de un usuario.
 * <p>
 * Tabla {@code swe_usrauth}, relación 1:1 con {@link SweUsrapl}. La contraseña se
 * almacena como hash BCrypt (ver {@code SecurityConfig#passwordEncoder}).
 */
@Entity
@Table(name = "swe_usrauth",
       uniqueConstraints = @UniqueConstraint(name = "swe_usrauth_id_usrapl_key", columnNames = "id_usrapl"))
@Getter
@Setter
@NoArgsConstructor
public class SweUsrauth extends SweAuditableEntity {

    /** Hash BCrypt de la contraseña; nunca el texto plano. */
    @NotBlank
    @Column(name = "password", nullable = false, length = 150)
    private String password;

    /**
     * Token de un solo uso del flujo "olvidé mi contraseña". Se genera en
     * {@code UserServiceImpl#forgotPassword} y se limpia al restablecer la contraseña.
     */
    @Column(name = "reset_password_token")
    private String resetPasswordToken;

    /** Vencimiento del token de recuperación. */
    @Column(name = "reset_password_expires")
    private LocalDateTime resetPasswordExpires;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usrapl", nullable = false)
    private SweUsrapl usrapl;
}
