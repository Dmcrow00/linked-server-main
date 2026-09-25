package com.linked.dto;

import jakarta.validation.constraints.NotBlank;

/** Contratos del chat de un turno ({@code /api/turnos/{id}/mensajes}). */
public class TurnoMensajeDto {

    public record CreateRequest(
            @NotBlank String texto
    ) {}

    /** @param creationTimestamp fecha de envío en ISO-8601, para ordenar y mostrar el chat */
    public record Response(
            Long id,
            String texto,
            boolean leido,
            Long usuarioEnviaId,
            String usuarioEnviaUsername,
            String creationTimestamp
    ) {}
}
