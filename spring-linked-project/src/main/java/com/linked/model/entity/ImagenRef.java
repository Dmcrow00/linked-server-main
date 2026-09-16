package com.linked.model.entity;

import com.core.model.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Imagen de referencia que adjunta el cliente al solicitar un turno.
 * <p>
 * Tabla {@code tat_imagen_ref}. {@code imagePath} guarda la URL de la imagen.
 */
@Entity
@Table(name = "tat_imagen_ref")
@Getter
@Setter
@NoArgsConstructor
public class ImagenRef extends BaseEntity {

    @NotBlank
    @Column(name = "image_path", nullable = false, columnDefinition = "TEXT")
    private String imagePath;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_turno_sesion", nullable = false)
    private TurnoSesion turnoSesion;
}
