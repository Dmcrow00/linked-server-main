package com.linked.controller;

import com.linked.dto.ApiResponse;
import com.linked.dto.SweItemmenuDto;
import com.linked.service.SweItemmenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

/** Administración del menú de navegación (solo Administrador). */
@RestController
@RequestMapping("/api/swe/menu")
@RequiredArgsConstructor
@PreAuthorize("hasRole('Administrador')")
@Tag(name = "SWE - Menú", description = "Gestión del menú de navegación")
public class SweItemmenuController {

    private final SweItemmenuService itemmenuService;

    @GetMapping
    @Operation(summary = "Listar items de menú raíz")
    public ResponseEntity<ApiResponse<List<SweItemmenuDto.Response>>> findAll() {
        return ResponseEntity.ok(ApiResponse.ok(itemmenuService.findAll()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener item de menú por ID")
    public ResponseEntity<ApiResponse<SweItemmenuDto.Response>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(itemmenuService.findById(id)));
    }

    @PostMapping
    @Operation(summary = "Crear item de menú")
    public ResponseEntity<ApiResponse<SweItemmenuDto.Response>> create(
            @Valid @RequestBody SweItemmenuDto.CreateRequest request) {
        SweItemmenuDto.Response response = itemmenuService.create(request);
        return ResponseEntity.created(URI.create("/api/swe/menu/" + response.id()))
                .body(ApiResponse.created(response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar item de menú")
    public ResponseEntity<ApiResponse<SweItemmenuDto.Response>> update(
            @PathVariable Long id,
            @Valid @RequestBody SweItemmenuDto.UpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(itemmenuService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar item de menú (soft delete)")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        itemmenuService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
