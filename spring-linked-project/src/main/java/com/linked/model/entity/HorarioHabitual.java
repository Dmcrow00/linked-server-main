package com.linked.model.entity;

import com.core.model.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;

/**
 * Franja horaria semanal en la que atiende un tatuador (una por día de la semana).
 * <p>
 * Tabla {@code tat_horario_habitual}. A partir de estas franjas se generan los horarios
 * disponibles para reservar (ver {@code AgendaServiceImpl}).
 */
@Entity
@Table(name = "tat_horario_habitual",
       uniqueConstraints = @UniqueConstraint(
           name = "tat_horario_habitual_unique",
           columnNames = {"id_tatuador", "dia_semana"}))
@Getter
@Setter
@NoArgsConstructor
public class HorarioHabitual extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_tatuador", nullable = false)
    private SweUsrapl tatuador;

    /** Día de la semana con la convención de JavaScript: 0 = domingo … 6 = sábado. */
    @Min(0)
    @Max(6)
    @Column(name = "dia_semana", nullable = false)
    private int diaSemana;

    @NotNull
    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @NotNull
    @Column(name = "hora_fin", nullable = false)
    private LocalTime horaFin;

    /** Duración de cada turno en minutos; define el intervalo entre horarios disponibles. */
    @Column(name = "duracion_turno_min", nullable = false)
    private int duracionTurnoMin;
}
