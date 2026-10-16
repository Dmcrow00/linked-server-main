package com.linked.controller;

import com.dmc00.core.swe.model.entity.SweUsrapl;
import com.dmc00.core.web.ApiResponse;
import com.linked.dto.SolicitudTatuadorDto;
import com.linked.security.Permisos;
import com.linked.service.SolicitudTatuadorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/** Revisión de solicitudes para ser tatuador (solo Administrador). */
@RestController
@RequestMapping("/api/solicitudes-tatuador")
@RequiredArgsConstructor
@Tag(name = "Solicitudes Tatuador", description = "Gestión de solicitudes para ser tatuador")
public class SolicitudTatuadorController {

    private final SolicitudTatuadorService solicitudService;

    @GetMapping
    @PreAuthorize(Permisos.ADMINISTRADOR)
    @Operation(summary = "Listar todas las solicitudes")
    public ResponseEntity<ApiResponse<List<SolicitudTatuadorDto.Response>>> getAll() {
        return ResponseEntity.ok(ApiResponse.ok(solicitudService.getAll()));
    }

    @PutMapping("/{id}/aprobar")
    @PreAuthorize(Permisos.ADMINISTRADOR)
    @Operation(summary = "Aprobar solicitud de tatuador")
    public ResponseEntity<ApiResponse<Void>> aprobar(
            @PathVariable Long id,
            @AuthenticationPrincipal SweUsrapl currentUser) {
        solicitudService.aprobar(id, currentUser.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PutMapping("/{id}/rechazar")
    @PreAuthorize(Permisos.ADMINISTRADOR)
    @Operation(summary = "Rechazar solicitud de tatuador")
    public ResponseEntity<ApiResponse<Void>> rechazar(
            @PathVariable Long id,
            @AuthenticationPrincipal SweUsrapl currentUser,
            @RequestBody(required = false) Map<String, String> body) {
        String notas = body != null ? body.get("notas") : null;
        solicitudService.rechazar(id, currentUser.getUsername(), notas);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
