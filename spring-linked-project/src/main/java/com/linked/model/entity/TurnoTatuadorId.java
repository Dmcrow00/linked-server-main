package com.linked.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/** Clave compuesta de {@link TurnoTatuador}: (id_tatuador, id_turno_sesion). */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class TurnoTatuadorId implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "id_tatuador")
    private Long idTatuador;

    @Column(name = "id_turno_sesion")
    private Long idTurnoSesion;
}
