package com.linked.dto;

import java.util.List;

/** Contratos de búsqueda de tatuadores ({@code /api/busqueda}). */
public class BusquedaDto {

    /**
     * Ficha pública de un tatuador para el mapa y el listado de búsqueda.
     * <p>
     * Solo lleva datos profesionales. {@code instagramHandle} es un dato de contacto, así que
     * viaja en {@code null} cuando la consulta llega sin sesión activa.
     *
     * @param lat latitud del estudio; la distancia al cliente la calcula el front
     */
    public record TatuadorUbicacion(
            Long id,
            String username,
            String profilePhoto,
            String estudio,
            String localidad,
            Double lat,
            Double lng,
            List<EspecialidadDto.Response> especialidades,
            String instagramHandle
    ) {}

    /**
     * @param estilosDetectados estilos reconocidos en las imágenes; vacío hasta que se integre la IA
     */
    public record EstiloBusquedaResponse(
            List<String> estilosDetectados,
            List<TatuadorUbicacion> tatuadores
    ) {}
}
