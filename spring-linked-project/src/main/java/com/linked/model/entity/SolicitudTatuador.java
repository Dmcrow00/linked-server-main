package com.linked.model.entity;

import com.core.model.entity.BaseEntity;
import com.linked.model.enums.SolicitudStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Solicitud de un usuario para ser habilitado como tatuador.
 * <p>
 * Tabla {@code tat_solicitud_tatuador}. Se crea al registrarse como tatuador: el usuario queda
 * como Cliente hasta que un administrador la aprueba y se le asigna el rol Tatuador
 * (ver {@code SolicitudTatuadorServiceImpl}).
 */
@Entity
@Table(name = "tat_solicitud_tatuador")
@Getter
@Setter
@NoArgsConstructor
public class SolicitudTatuador extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_user", nullable = false)
    private SweUsrapl user;

    @Size(max = 100)
    @Column(name = "estudio", length = 100)
    private String estudio;

    /**
     * IDs de las especialidades elegidas en el registro, como arreglo JSON (ej. {@code "[1, 3]"}).
     * Se asignan al tatuador recién cuando se aprueba la solicitud.
     * <p>
     * {@code @JdbcTypeCode(JSON)} es necesario porque la columna es {@code jsonb}: enviado como
     * texto común, PostgreSQL rechaza el INSERT por incompatibilidad de tipos.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "especialidades_ids", columnDefinition = "jsonb")
    private String especialidadesIds;

    /**
     * Estado del trámite. No confundir con {@code status} heredado de BaseEntity, que indica
     * baja lógica.
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "solicitud_status", nullable = false, length = 20)
    private SolicitudStatus solicitudStatus = SolicitudStatus.pendiente;

    @Column(name = "notas", columnDefinition = "TEXT")
    private String notas;

    /** Username del administrador que aprobó o rechazó la solicitud. */
    @Size(max = 100)
    @Column(name = "admin_user", length = 100)
    private String adminUser;
}
