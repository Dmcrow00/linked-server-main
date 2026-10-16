package com.linked.model.entity;

import com.dmc00.core.data.BaseEntity;
import com.dmc00.core.swe.model.entity.SweUsrapl;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Mensaje del chat de un turno, entre el cliente y el tatuador asignado.
 * <p>
 * Tabla {@code tat_turno_mensajes}. La fecha de envío es {@code creationTimestamp},
 * que completa la auditoría de BaseEntity.
 */
@Entity
@Table(name = "tat_turno_mensajes")
@Getter
@Setter
@NoArgsConstructor
public class TurnoMensaje extends BaseEntity {

    @NotBlank
    @Column(name = "texto", nullable = false, columnDefinition = "TEXT")
    private String texto;

    @Column(name = "leido", nullable = false)
    private boolean leido = false;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_turno_sesion", nullable = false)
    private TurnoSesion turnoSesion;

    /** Autor del mensaje: puede ser el cliente o el tatuador del turno. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario_envia")
    private SweUsrapl usuarioEnvia;
}
