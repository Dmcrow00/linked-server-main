package com.linked.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class EspecialidadDto {

    public record CreateRequest(
            @NotBlank @Size(max = 100) String nombre,
            String descripcion
    ) {}

    public record UpdateRequest(
            @Size(max = 100) String nombre,
            String descripcion
    ) {}

    public record Response(
            Long id,
            String nombre,
            String descripcion,
            String status
    ) {}
}
