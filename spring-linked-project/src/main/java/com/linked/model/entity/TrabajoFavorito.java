package com.linked.model.entity;

import com.dmc00.core.data.BaseEntity;
import com.dmc00.core.swe.model.entity.SweUsrapl;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * "Me gusta" de un cliente sobre un trabajo.
 * <p>
 * Tabla {@code tat_trabajo_favorito}. La restricción única (cliente, trabajo) impide
 * marcar dos veces el mismo trabajo.
 */
@Entity
@Table(name = "tat_trabajo_favorito",
       uniqueConstraints = @UniqueConstraint(
           name = "tat_trabajo_favorito_unique",
           columnNames = {"id_cliente", "id_trabajo"}))
@Getter
@Setter
@NoArgsConstructor
public class TrabajoFavorito extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_cliente", nullable = false)
    private SweUsrapl cliente;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_trabajo", nullable = false)
    private Trabajo trabajo;
}
