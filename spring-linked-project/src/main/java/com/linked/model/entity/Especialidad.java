package com.linked.model.entity;

import com.core.model.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Estilo de tatuaje (Realismo, Blackwork, Lettering, etc.).
 * <p>
 * Tabla {@code tat_especialidad}. Se asocia a los tatuadores mediante la tabla intermedia
 * {@code tat_tatuador_especialidad} (ver {@link SweUsrapl#getEspecialidades()}).
 * La baja es lógica: {@code status = "false"}.
 */
@Entity
@Table(name = "tat_especialidad",
       uniqueConstraints = @UniqueConstraint(name = "tat_especialidad_nombre_key", columnNames = "nombre"))
@Getter
@Setter
@NoArgsConstructor
public class Especialidad extends BaseEntity {

    @NotBlank(message = "El nombre de la especialidad es obligatorio")
    @Size(max = 100)
    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;
}
