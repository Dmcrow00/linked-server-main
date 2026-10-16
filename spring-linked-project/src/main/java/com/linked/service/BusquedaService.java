package com.linked.service;

import com.linked.dto.BusquedaDto;

import java.util.List;

/**
 * Búsqueda pública de tatuadores. Responde la ficha reducida de {@link BusquedaDto.TatuadorUbicacion}:
 * datos profesionales y coordenadas, sin nombre real, email ni horarios.
 */
public interface BusquedaService {

    /**
     * Tatuadores con coordenadas cargadas.
     *
     * @param conSesion el handle de Instagram es un dato de contacto: solo viaja con sesión
     */
    List<BusquedaDto.TatuadorUbicacion> tatuadoresConUbicacion(boolean conSesion);

    /**
     * Tatuadores sugeridos para un estilo. La detección de estilos en las imágenes todavía no está:
     * por ahora devuelve los tatuadores dentro del radio y {@code estilosDetectados} vacío.
     *
     * @param radioKm sin valor, 50 km
     */
    BusquedaDto.EstiloBusquedaResponse porEstilo(Double lat, Double lng, Double radioKm, boolean conSesion);
}
