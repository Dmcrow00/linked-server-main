package com.linked.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tat_turno_tatuador")
@Getter
@Setter
@NoArgsConstructor
public class TurnoTatuador {

    @EmbeddedId
    private TurnoTatuadorId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("idTatuador")
    @JoinColumn(name = "id_tatuador", nullable = false)
    private SweUsrapl tatuador;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("idTurnoSesion")
    @JoinColumn(name = "id_turno_sesion", nullable = false)
    private TurnoSesion turnoSesion;

    public TurnoTatuador(SweUsrapl tatuador, TurnoSesion turnoSesion) {
        this.id = new TurnoTatuadorId(tatuador.getId(), turnoSesion.getId());
        this.tatuador = tatuador;
        this.turnoSesion = turnoSesion;
    }
}
