package com.linked.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Contratos de especialidades ({@code /api/especialidades}). */
public class EspecialidadDto {

    public record CreateRequest(
            @NotBlank @Size(max = 100) String nombre,
            String descripcion
    ) {}

    /** Actualización parcial: los campos {@code null} no se modifican. */
    public record UpdateRequest(
            @Size(max = 100) String nombre,
            String descripcion
    ) {}

    /** @param status {@code "true"} activa, {@code "false"} dada de baja */
    public record Response(
            Long id,
            String nombre,
            String descripcion,
            String status
    ) {}
}
