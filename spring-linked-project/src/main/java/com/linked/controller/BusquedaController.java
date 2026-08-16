package com.linked.controller;

import com.linked.dto.ApiResponse;
import com.linked.dto.BusquedaDto;
import com.linked.repository.SweUsrRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/busqueda")
@RequiredArgsConstructor
@Tag(name = "Búsqueda", description = "Búsqueda de tatuadores por ubicación y estilo")
public class BusquedaController {

    private final SweUsrRepository sweUsrRepository;

    @GetMapping("/tatuadores-ubicacion")
    @Operation(summary = "Tatuadores activos con coordenadas geográficas")
    public ResponseEntity<ApiResponse<List<BusquedaDto.TatuadorUbicacion>>> getTatuadoresUbicacion() {
        List<BusquedaDto.TatuadorUbicacion> result = sweUsrRepository.findAllTatuadores().stream()
                .filter(u -> u.getLat() != null && u.getLng() != null)
                .map(u -> new BusquedaDto.TatuadorUbicacion(
                        u.getId(), u.getUsername(), u.getProfilePhoto(),
                        u.getLocalidad(), u.getLat(), u.getLng()))
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @PostMapping("/por-estilo")
    @Operation(summary = "Búsqueda de tatuadores por imagen de estilo (stub — IA pendiente)")
    public ResponseEntity<ApiResponse<BusquedaDto.EstiloBusquedaResponse>> buscarPorEstilo(
            @RequestBody Map<String, Object> request) {
        // TODO: integrar servicio de detección de estilos con IA
        List<BusquedaDto.TatuadorUbicacion> tatuadores = sweUsrRepository.findAllTatuadores().stream()
                .filter(u -> u.getLat() != null && u.getLng() != null)
                .map(u -> new BusquedaDto.TatuadorUbicacion(
                        u.getId(), u.getUsername(), u.getProfilePhoto(),
                        u.getLocalidad(), u.getLat(), u.getLng()))
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(
                new BusquedaDto.EstiloBusquedaResponse(List.of(), tatuadores)));
    }
}
