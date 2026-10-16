package com.linked.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

/**
 * Configuración de la integración con Instagram ({@code app.instagram.*}).
 *
 * @param appId           id de la app de Meta ({@code META_APP_ID})
 * @param appSecret       secreto de la app de Meta ({@code META_APP_SECRET})
 * @param redirectUri     URL de {@code /api/instagram/callback} registrada en Meta
 * @param graphApiVersion versión de la Graph API
 * @param sincronizacion  tarea programada que importa las publicaciones
 */
@ConfigurationProperties(prefix = "app.instagram")
public record PropiedadesInstagram(
        String appId,
        String appSecret,
        @DefaultValue("http://localhost:8080/api/instagram/callback") String redirectUri,
        @DefaultValue("v21.0") String graphApiVersion,
        @DefaultValue Sincronizacion sincronizacion) {

    /**
     * @param cada                frecuencia de la sincronización (formato ISO-8601, p. ej. {@code PT2H}:
     *                            lo lee también {@code @Scheduled})
     * @param demoraInicial       espera desde el arranque hasta la primera ejecución
     * @param renovarAntesDe      se renueva el token cuando le queda menos que esto
     * @param desvincularVencidos si un token vencido desvincula la cuenta (en dev no: el servidor no
     *                            corre siempre y el token puede vencer entre arranques)
     */
    public record Sincronizacion(
            @DefaultValue("PT2H") Duration cada,
            @DefaultValue("PT1M") Duration demoraInicial,
            @DefaultValue("30d") Duration renovarAntesDe,
            @DefaultValue("true") boolean desvincularVencidos) {
    }
}
