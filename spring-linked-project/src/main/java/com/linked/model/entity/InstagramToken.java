package com.linked.model.entity;

import com.core.model.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Token de acceso a la API de Instagram de un tatuador que vinculó su cuenta.
 * <p>
 * Tabla {@code tat_instagram_token}, uno por tatuador. Se guarda el token de larga duración
 * (unos 60 días) y una tarea programada lo renueva antes de que venza
 * (ver {@code InstagramServiceImpl#refreshExpiringTokens}).
 */
@Entity
@Table(name = "tat_instagram_token",
       uniqueConstraints = @UniqueConstraint(
           name = "tat_instagram_token_tatuador_unique",
           columnNames = "id_tatuador"))
@Getter
@Setter
@NoArgsConstructor
public class InstagramToken extends BaseEntity {

    @NotNull
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_tatuador", nullable = false, unique = true)
    private SweUsrapl tatuador;

    @NotBlank
    @Size(max = 100)
    @Column(name = "instagram_user_id", nullable = false, length = 100)
    private String instagramUserId;

    @NotBlank
    @Column(name = "access_token", nullable = false, columnDefinition = "TEXT")
    private String accessToken;

    @NotNull
    @Column(name = "token_expires_at", nullable = false)
    private LocalDateTime tokenExpiresAt;
}
