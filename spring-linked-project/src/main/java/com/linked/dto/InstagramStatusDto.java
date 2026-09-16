package com.linked.dto;

/**
 * Estado de vinculación con Instagram del tatuador autenticado.
 *
 * @param expiresAt vencimiento del token en formato ISO-8601; {@code null} si no está conectado
 */
public record InstagramStatusDto(
        boolean connected,
        String instagramUserId,
        String expiresAt
) {}
