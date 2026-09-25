package com.linked.dto;

import java.util.List;

/** Contratos de trabajos del portfolio ({@code /api/trabajos}). */
public class TrabajoDto {

    public record CreateRequest(
            String descripcion
    ) {}

    /**
     * @param fotos      URLs de las fotos del trabajo
     * @param tatuador   vista pública del autor: el feed se sirve sin sesión, así que no puede
     *                   arrastrar los datos personales ni de contacto del tatuador
     * @param likesCount cantidad de likes recibidos
     * @param likedByMe  si el usuario autenticado le dio like; siempre {@code false} sin sesión
     */
    public record Response(
            Long id,
            String descripcion,
            String instagramMediaId,
            List<String> fotos,
            UserDto.Publico tatuador,
            int likesCount,
            boolean likedByMe,
            String creationTimestamp,
            String status
    ) {}
}
