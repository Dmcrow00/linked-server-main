package com.linked.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Contratos de administración del menú de navegación ({@code /api/swe/menu}).
 * En todos los casos {@code rolesPermitidos} es una lista de roles separados por coma;
 * vacío significa visible para todos.
 */
public class SweItemmenuDto {

    public record CreateRequest(
            Long idSupitemmenu,
            @NotBlank @Size(max = 100) String title,
            @Size(max = 100) String icon,
            @Size(max = 255) String route,
            Integer orderNumber,
            @Size(max = 500) String rolesPermitidos
    ) {}

    public record UpdateRequest(
            Long idSupitemmenu,
            @Size(max = 100) String title,
            @Size(max = 100) String icon,
            @Size(max = 255) String route,
            Integer orderNumber,
            @Size(max = 500) String rolesPermitidos
    ) {}

    public record Response(
            Long id,
            Long idSupitemmenu,
            String title,
            String icon,
            String route,
            Integer orderNumber,
            String rolesPermitidos,
            String status
    ) {}
}
