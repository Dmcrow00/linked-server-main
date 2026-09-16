package com.linked.dto;

import java.util.List;

/** Contratos de búsqueda de tatuadores ({@code /api/busqueda}). */
public class BusquedaDto {

    public record TatuadorUbicacion(
            Long id,
            String username,
            String profilePhoto,
            String localidad,
            Double lat,
            Double lng
    ) {}

    public record EstiloBusquedaResponse(
            List<String> estilosDetectados,
            List<TatuadorUbicacion> tatuadores
    ) {}
}
