package com.linked.service;

import com.linked.dto.InstagramStatusDto;

/**
 * Integración con Instagram: vinculación de la cuenta del tatuador por OAuth e importación
 * de sus posts como trabajos del portfolio.
 */
public interface InstagramService {

    /** URL de autorización de Instagram; el ID del tatuador viaja en {@code state} para el callback. */
    String initiateOAuth(Long tatuadorId);

    /** Canjea el código de autorización por un token de larga duración y lo guarda para el tatuador. */
    void handleCallback(String code, String state);

    /**
     * Importa como trabajos las imágenes y carruseles que todavía no se importaron. Los errores
     * de la API se registran en el log y no se propagan.
     */
    void syncPosts(Long tatuadorId);

    /** Igual que {@link #syncPosts} pero en segundo plano; se usa al iniciar sesión. */
    void syncAsync(Long tatuadorId);

    InstagramStatusDto getStatus(Long tatuadorId);

    /** Elimina el token guardado; los trabajos ya importados se conservan. */
    void disconnect(Long tatuadorId);
}
