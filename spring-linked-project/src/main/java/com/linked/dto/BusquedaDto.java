package com.linked.dto;

import java.util.List;

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
