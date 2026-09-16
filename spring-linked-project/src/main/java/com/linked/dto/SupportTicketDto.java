package com.linked.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

/** Contratos de tickets de soporte ({@code /api/supportTicket}). */
public class SupportTicketDto {

    public record CreateRequest(
            @NotBlank String description,
            @Size(max = 50) String category,
            @Size(max = 20) String priority,
            @Size(max = 500) String urlPagina,
            @Size(max = 500) String userAgent,
            String screenshot,
            @Email @Size(max = 254) String contactEmail
    ) {}

    /**
     * Actualización parcial: los campos {@code null} no se modifican.
     *
     * @param status {@code true} para dejar el ticket abierto, {@code false} para cerrarlo
     */
    public record UpdateRequest(
            Boolean status,
            @Size(max = 50) String category,
            @Size(max = 20) String priority,
            String adminResponse
    ) {}

    /**
     * @param status   {@code true} si el ticket está abierto
     * @param usuarios usernames vinculados al ticket
     */
    public record Response(
            Long id,
            boolean status,
            String description,
            String category,
            String priority,
            String urlPagina,
            String userAgent,
            String screenshot,
            String adminResponse,
            String contactEmail,
            List<String> usuarios,
            String creationTimestamp,
            String modificationTimestamp
    ) {}
}
