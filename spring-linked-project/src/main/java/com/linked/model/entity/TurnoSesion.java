package com.linked.model.entity;

import com.core.model.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tat_turno_sesion")
@Getter
@Setter
@NoArgsConstructor
public class TurnoSesion extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cliente")
    private SweUsrapl cliente;

    @Column(name = "estado", nullable = false, length = 20)
    private String estado = "Pendiente";

    @Column(name = "fecha_hora_inicio", nullable = false)
    private LocalDateTime fechaHoraInicio;

    @Column(name = "fecha_hora_fin", nullable = false)
    private LocalDateTime fechaHoraFin;

    @Size(max = 1000)
    @Column(name = "descripcion_cliente", length = 1000)
    private String descripcionCliente;

    @OneToMany(mappedBy = "turnoSesion", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<TurnoTatuador> turnoTatuadores = new ArrayList<>();

    @OneToMany(mappedBy = "turnoSesion", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ImagenRef> imagenesRef = new ArrayList<>();

    @OneToMany(mappedBy = "turnoSesion", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<TurnoMensaje> mensajes = new ArrayList<>();
}
