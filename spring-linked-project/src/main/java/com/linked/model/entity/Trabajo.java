package com.linked.model.entity;

import com.core.model.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Trabajo publicado en el portfolio de un tatuador: una o más fotos con descripción.
 * <p>
 * Tabla {@code tat_trabajo}. Puede cargarse a mano o importarse desde Instagram; en ese
 * caso {@code instagramMediaId} evita importar dos veces el mismo post.
 */
@Entity
@Table(name = "tat_trabajo")
@Getter
@Setter
@NoArgsConstructor
public class Trabajo extends BaseEntity {

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_tatuador", nullable = false)
    private SweUsrapl tatuador;

    /** ID del post de Instagram de origen; {@code null} si el trabajo se cargó a mano. */
    @Size(max = 100)
    @Column(name = "instagram_media_id", length = 100)
    private String instagramMediaId;

    @OneToMany(mappedBy = "trabajo", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<TrabajoFoto> fotos = new ArrayList<>();

    /** Likes de clientes; la cantidad se expone como {@code likesCount}. */
    @OneToMany(mappedBy = "trabajo", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<TrabajoFavorito> favoritos = new ArrayList<>();
}
