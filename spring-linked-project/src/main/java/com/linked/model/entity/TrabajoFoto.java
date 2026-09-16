package com.linked.model.entity;

import com.core.model.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Foto de un trabajo del portfolio.
 * <p>
 * Tabla {@code tat_trabajo_foto}. {@code imagePath} guarda la URL de la imagen.
 */
@Entity
@Table(name = "tat_trabajo_foto")
@Getter
@Setter
@NoArgsConstructor
public class TrabajoFoto extends BaseEntity {

    @NotBlank
    @Column(name = "image_path", nullable = false, columnDefinition = "TEXT")
    private String imagePath;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_trabajo", nullable = false)
    private Trabajo trabajo;
}
