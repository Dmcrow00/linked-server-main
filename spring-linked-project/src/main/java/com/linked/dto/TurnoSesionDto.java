package com.linked.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Contratos de turnos ({@code /api/turnos}).
 * Las fechas viajan en formato ISO-8601 local, sin zona horaria (ej. {@code 2026-09-15T14:30:00}).
 */
public class TurnoSesionDto {

    public record CreateRequest(
            @NotNull Long tatuadorId,
            @NotNull String fechaHoraInicio,
            @NotNull String fechaHoraFin,
            String descripcionCliente
    ) {}

    /** @param estado uno de los valores de {@link com.linked.model.enums.EstadoTurno} */
    public record GestionarRequest(
            @NotNull String estado
    ) {}

    /** Actualización parcial: los campos {@code null} no se modifican. */
    public record UpdateRequest(
            String fechaHoraInicio,
            String fechaHoraFin,
            String descripcionCliente,
            String estado
    ) {}

    /** @param imagenesRef URLs de las imágenes de referencia que adjuntó el cliente */
    public record Response(
            Long id,
            UserDto.Response cliente,
            String estado,
            String fechaHoraInicio,
            String fechaHoraFin,
            String descripcionCliente,
            List<UserDto.Response> tatuadores,
            List<String> imagenesRef,
            List<TurnoMensajeDto.Response> mensajes,
            String creationTimestamp
    ) {}
}
