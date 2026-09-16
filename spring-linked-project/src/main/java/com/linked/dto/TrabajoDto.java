package com.linked.dto;

import java.util.List;

/** Contratos de trabajos del portfolio ({@code /api/trabajos}). */
public class TrabajoDto {

    public record CreateRequest(
            String descripcion
    ) {}

    /**
     * @param fotos      URLs de las fotos del trabajo
     * @param likesCount cantidad de likes recibidos
     * @param likedByMe  si el usuario autenticado le dio like; siempre {@code false} sin sesión
     */
    public record Response(
            Long id,
            String descripcion,
            String instagramMediaId,
            List<String> fotos,
            UserDto.Response tatuador,
            int likesCount,
            boolean likedByMe,
            String creationTimestamp,
            String status
    ) {}
}
