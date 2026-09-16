package com.linked.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Contratos de roles de usuario. */
public class SweRolDto {

    public record CreateRequest(
            @NotBlank @Size(max = 50) String description
    ) {}

    public record UpdateRequest(
            @Size(max = 50) String description
    ) {}

    public record Response(
            Long id,
            String description,
            String status
    ) {}
}
