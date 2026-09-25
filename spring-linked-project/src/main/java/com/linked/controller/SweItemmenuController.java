package com.linked.controller;

import com.linked.dto.ApiResponse;
import com.linked.dto.SweItemmenuDto;
import com.linked.model.entity.SweUsrapl;
import com.linked.service.SweItemmenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

/**
 * Menú de navegación. La lectura la hace cualquier usuario autenticado —el servicio filtra los
 * ítems según sus roles— y la administración queda reservada al Administrador.
 */
@RestController
@RequestMapping("/api/swe/menu")
@RequiredArgsConstructor
@Tag(name = "SWE - Menú", description = "Gestión del menú de navegación")
public class SweItemmenuController {

    private final SweItemmenuService itemmenuService;

    @GetMapping
    @Operation(summary = "Ítems de menú visibles para el usuario autenticado")
    public ResponseEntity<ApiResponse<List<SweItemmenuDto.Response>>> findAll(
            @AuthenticationPrincipal SweUsrapl currentUser) {
        List<Long> roleIds = currentUser.getUserRoles().stream()
                .filter(ur -> "true".equals(ur.getStatus()))
                .map(ur -> ur.getRolapl().getId())
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(itemmenuService.findAllVisibles(roleIds)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('Administrador')")
    @Operation(summary = "Obtener item de menú por ID")
    public ResponseEntity<ApiResponse<SweItemmenuDto.Response>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(itemmenuService.findById(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('Administrador')")
    @Operation(summary = "Crear item de menú")
    public ResponseEntity<ApiResponse<SweItemmenuDto.Response>> create(
            @Valid @RequestBody SweItemmenuDto.CreateRequest request) {
        SweItemmenuDto.Response response = itemmenuService.create(request);
        return ResponseEntity.created(URI.create("/api/swe/menu/" + response.id()))
                .body(ApiResponse.created(response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('Administrador')")
    @Operation(summary = "Actualizar item de menú")
    public ResponseEntity<ApiResponse<SweItemmenuDto.Response>> update(
            @PathVariable Long id,
            @Valid @RequestBody SweItemmenuDto.UpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(itemmenuService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('Administrador')")
    @Operation(summary = "Eliminar item de menú (soft delete)")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        itemmenuService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
