package com.linked.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class HorarioHabitualDto {

    public record SaveRequest(
            @NotNull List<DiaHorario> horarios
    ) {}

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
