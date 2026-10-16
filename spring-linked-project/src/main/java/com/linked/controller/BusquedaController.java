package com.linked.controller;

import com.dmc00.core.swe.model.entity.SweUsrapl;
import com.dmc00.core.web.ApiResponse;
import com.linked.dto.BusquedaDto;
import com.linked.security.Permisos;
import com.linked.service.BusquedaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Búsqueda de tatuadores por ubicación y, a futuro, por estilo a partir de una imagen. Es el único
 * listado de tatuadores accesible sin sesión.
 */
@RestController
@RequestMapping("/api/busqueda")
@RequiredArgsConstructor
@Tag(name = "Búsqueda", description = "Búsqueda de tatuadores por ubicación y estilo")
public class BusquedaController {

    private final BusquedaService busquedaService;

    @GetMapping("/tatuadores-ubicacion")
    @PreAuthorize(Permisos.PUBLICO)
    @Operation(summary = "Tatuadores activos con coordenadas geográficas")
    public ResponseEntity<ApiResponse<List<BusquedaDto.TatuadorUbicacion>>> getTatuadoresUbicacion(
            @AuthenticationPrincipal SweUsrapl usuario) {
        return ResponseEntity.ok(ApiResponse.ok(busquedaService.tatuadoresConUbicacion(usuario != null)));
    }

    /**
     * Llega como {@code multipart/form-data} porque transporta las imágenes de referencia, que por
     * ahora no se analizan (la detección de estilos con IA está pendiente).
     */
    @PostMapping(value = "/por-estilo", consumes = "multipart/form-data")
    @PreAuthorize(Permisos.PUBLICO)
    @Operation(summary = "Búsqueda de tatuadores por imagen de estilo (detección con IA pendiente)")
    public ResponseEntity<ApiResponse<BusquedaDto.EstiloBusquedaResponse>> buscarPorEstilo(
            @RequestParam(value = "imagenes", required = false) MultipartFile[] imagenes,
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lng,
            @RequestParam(required = false) Double radioKm,
            @AuthenticationPrincipal SweUsrapl usuario) {
        return ResponseEntity.ok(ApiResponse.ok(busquedaService.porEstilo(lat, lng, radioKm, usuario != null)));
    }
}
