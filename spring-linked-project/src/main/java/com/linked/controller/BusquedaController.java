package com.linked.controller;

import com.linked.dto.ApiResponse;
import com.linked.dto.BusquedaDto;
import com.linked.dto.EspecialidadDto;
import com.linked.model.entity.SweUsrapl;
import com.linked.repository.SweUsrRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Búsqueda de tatuadores por ubicación y, a futuro, por estilo a partir de una imagen.
 * <p>
 * Es el único listado de tatuadores accesible sin sesión, por lo que responde la ficha
 * reducida de {@link BusquedaDto.TatuadorUbicacion}: datos profesionales y coordenadas, sin
 * nombre real, email ni horarios. El contacto solo se agrega si hay sesión activa.
 */
@RestController
@RequestMapping("/api/busqueda")
@RequiredArgsConstructor
@Tag(name = "Búsqueda", description = "Búsqueda de tatuadores por ubicación y estilo")
public class BusquedaController {

    /** Radio de búsqueda por defecto cuando el cliente no indica uno, en kilómetros. */
    private static final double RADIO_KM_POR_DEFECTO = 50;
    private static final double RADIO_TIERRA_KM = 6371;

    private final SweUsrRepository sweUsrRepository;

    @GetMapping("/tatuadores-ubicacion")
    @Operation(summary = "Tatuadores activos con coordenadas geográficas")
    public ResponseEntity<ApiResponse<List<BusquedaDto.TatuadorUbicacion>>> getTatuadoresUbicacion() {
        return ResponseEntity.ok(ApiResponse.ok(buscarTatuadores(null, null, 0)));
    }

    /**
     * Recibe las imágenes de referencia del cliente y devuelve tatuadores sugeridos.
     * <p>
     * Llega como {@code multipart/form-data} porque transporta archivos. La detección de
     * estilos todavía no está implementada: por ahora se responde la lista filtrada por
     * cercanía y {@code estilosDetectados} vacío, que es lo que el cliente ya sabe manejar.
     */
    @PostMapping(value = "/por-estilo", consumes = "multipart/form-data")
    @Operation(summary = "Búsqueda de tatuadores por imagen de estilo (detección con IA pendiente)")
    public ResponseEntity<ApiResponse<BusquedaDto.EstiloBusquedaResponse>> buscarPorEstilo(
            @RequestParam(value = "imagenes", required = false) MultipartFile[] imagenes,
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lng,
            @RequestParam(required = false) Double radioKm) {
        // TODO: derivar los estilos de 'imagenes' con IA y quedarse con los tatuadores cuya
        // especialidad coincida, ordenados por afinidad.
        double radio = radioKm != null ? radioKm : RADIO_KM_POR_DEFECTO;
        List<BusquedaDto.TatuadorUbicacion> tatuadores = buscarTatuadores(lat, lng, radio);
        return ResponseEntity.ok(ApiResponse.ok(
                new BusquedaDto.EstiloBusquedaResponse(List.of(), tatuadores)));
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    /** Tatuadores con coordenadas cargadas; si se indica una zona, descarta los que quedan fuera. */
    private List<BusquedaDto.TatuadorUbicacion> buscarTatuadores(Double lat, Double lng, double radioKm) {
        boolean filtrarPorZona = lat != null && lng != null && radioKm > 0;
        boolean conSesion = haySesionActiva();

        return sweUsrRepository.findAllTatuadoresConEspecialidades().stream()
                .filter(u -> u.getLat() != null && u.getLng() != null)
                .filter(u -> !filtrarPorZona
                        || distanciaKm(lat, lng, u.getLat(), u.getLng()) <= radioKm)
                .map(u -> new BusquedaDto.TatuadorUbicacion(
                        u.getId(),
                        u.getUsername(),
                        u.getProfilePhoto(),
                        u.getEstudio(),
                        u.getLocalidad(),
                        u.getLat(),
                        u.getLng(),
                        mapEspecialidades(u),
                        conSesion ? u.getInstagramHandle() : null))
                .toList();
    }

    private List<EspecialidadDto.Response> mapEspecialidades(SweUsrapl u) {
        if (u.getEspecialidades() == null) return List.of();
        return u.getEspecialidades().stream()
                .map(e -> new EspecialidadDto.Response(
                        e.getId(), e.getNombre(), e.getDescripcion(), e.getStatus()))
                .toList();
    }

    /** Distancia entre dos coordenadas por la fórmula de Haversine, en kilómetros. */
    private double distanciaKm(double lat1, double lng1, double lat2, double lng2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return RADIO_TIERRA_KM * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    /** Estos endpoints son públicos, así que hay que preguntar si la request trajo sesión. */
    private boolean haySesionActiva() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.isAuthenticated()
                && auth.getPrincipal() instanceof SweUsrapl;
    }
}
