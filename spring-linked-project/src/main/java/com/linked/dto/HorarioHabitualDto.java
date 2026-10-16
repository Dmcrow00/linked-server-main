package com.linked.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/** Contratos del horario habitual de un tatuador ({@code /api/agenda/horario-habitual}). */
public class HorarioHabitualDto {

    /** Reemplaza el horario completo: los días que no se envían quedan sin atención. */
    public record SaveRequest(
            @NotNull List<DiaHorario> horarios
    ) {}

    /**
     * @param diaSemana        0 = domingo … 6 = sábado
     * @param horaInicio       hora en formato ISO ({@code HH:mm})
     * @param horaFin          hora en formato ISO ({@code HH:mm})
     * @param duracionTurnoMin duración de cada turno en minutos
     */
    public record DiaHorario(
            @Min(0) @Max(6) int diaSemana,
            @NotNull String horaInicio,
            @NotNull String horaFin,
            @Min(15) int duracionTurnoMin
    ) {}

    public record Response(
            Long id,
            int diaSemana,
            String horaInicio,
            String horaFin,
            int duracionTurnoMin
    ) {}
}
