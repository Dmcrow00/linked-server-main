package com.linked.model.entity;

import com.core.model.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario_envia")
    private SweUsrapl usuarioEnvia;

    @PrePersist
    protected void onCreate() {
    }
}
