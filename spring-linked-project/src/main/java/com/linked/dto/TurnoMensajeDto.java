package com.linked.dto;

import jakarta.validation.constraints.NotBlank;

/** Contratos del chat de un turno ({@code /api/turnos/{id}/mensajes}). */
public class TurnoMensajeDto {

    public record CreateRequest(
            @NotBlank String texto
    ) {}

    public record Response(
            Long id,
            String texto,
            boolean leido,
            Long usuarioEnviaId,
            String usuarioEnviaUsername
    ) {}
}
