package com.linked.dto;

import jakarta.validation.constraints.NotBlank;

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
