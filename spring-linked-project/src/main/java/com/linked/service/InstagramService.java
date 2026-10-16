package com.linked.service;

import com.linked.dto.InstagramStatusDto;
import com.linked.dto.SincronizacionInstagramDto;

import java.util.List;

/**
 * Integración con Instagram: vinculación de la cuenta del tatuador por OAuth e importación de sus
 * publicaciones como trabajos del portfolio.
 */
public interface InstagramService {

    /** URL de autorización de Instagram; el {@code state} lleva el id del tatuador firmado y con vencimiento. */
    String initiateOAuth(Long tatuadorId);

    /**
     * Canjea el código por un token de larga duración, lo guarda y copia usuario y foto de
     * Instagram al perfil. Publica {@link CuentaVinculada} para importar las publicaciones.
     */
    void handleCallback(String code, String state);

    /**
     * Importa las publicaciones nuevas (imágenes y carruseles) y da de baja los trabajos cuya
     * publicación se borró en Instagram. Si el token venció, desvincula la cuenta y responde 409.
     */
    SincronizacionInstagramDto syncPosts(Long tatuadorId);

    InstagramStatusDto getStatus(Long tatuadorId);

    /** Borra el token y limpia el usuario y la foto de Instagram del perfil; los trabajos importados quedan. */
    void disconnect(Long tatuadorId);

    /** Tatuadores con una cuenta vinculada. */
    List<Long> tatuadoresVinculados();

    /**
     * Lo que hace la tarea programada con cada cuenta, como el scheduler de Node: si el token venció
     * desvincula (según {@code app.instagram.sincronizacion.desvincular-vencidos}); si vence pronto lo
     * renueva; después sincroniza. Si el tatuador no tiene cuenta vinculada no hace nada.
     */
    void mantener(Long tatuadorId);

    /** Se publica cuando un tatuador termina de vincular su cuenta. */
    record CuentaVinculada(Long tatuadorId) {
    }
}
