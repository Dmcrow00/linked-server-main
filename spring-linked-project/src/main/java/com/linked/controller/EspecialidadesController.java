package com.linked.controller;

import com.dmc00.core.web.ApiResponse;
import com.linked.dto.EspecialidadDto;
import com.linked.security.Permisos;
import com.linked.service.EspecialidadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

/** Consulta pública de especialidades y su administración (solo Administrador). */
@RestController
@RequestMapping("/api/especialidades")
@RequiredArgsConstructor
@Tag(name = "Especialidades", description = "Gestión de especialidades de tatuaje")
public class EspecialidadesController {

    private final EspecialidadService especialidadService;

    @GetMapping
    @PreAuthorize(Permisos.PUBLICO)
    @Operation(summary = "Listar especialidades activas")
    public ResponseEntity<ApiResponse<List<EspecialidadDto.Response>>> findAll() {
        return ResponseEntity.ok(ApiResponse.ok(especialidadService.findAll()));
    }

    @GetMapping("/{id}")
    @PreAuthorize(Permisos.PUBLICO)
    @Operation(summary = "Obtener especialidad por ID")
    public ResponseEntity<ApiResponse<EspecialidadDto.Response>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(especialidadService.findById(id)));
    }

    @PostMapping
    @PreAuthorize(Permisos.ADMINISTRADOR)
    @Operation(summary = "Crear especialidad")
    public ResponseEntity<ApiResponse<EspecialidadDto.Response>> create(
            @Valid @RequestBody EspecialidadDto.CreateRequest request) {
        EspecialidadDto.Response response = especialidadService.create(request);
        return ResponseEntity.created(URI.create("/api/especialidades/" + response.id()))
                .body(ApiResponse.created(response));
    }

    @PutMapping("/{id}")
    @PreAuthorize(Permisos.ADMINISTRADOR)
    @Operation(summary = "Actualizar especialidad")
    public ResponseEntity<ApiResponse<EspecialidadDto.Response>> update(
            @PathVariable Long id,
            @Valid @RequestBody EspecialidadDto.UpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(especialidadService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize(Permisos.ADMINISTRADOR)
    @Operation(summary = "Eliminar especialidad (soft delete)")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        especialidadService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
