package com.linked.controller;

import com.dmc00.core.web.ApiResponse;
import com.linked.dto.EspecialidadDto;
import com.linked.security.Permisos;
import com.linked.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Especialidades asignadas a cada tatuador. */
@RestController
@RequestMapping("/api/tatuador")
@RequiredArgsConstructor
@Tag(name = "Tatuador - Especialidades", description = "Asignación de especialidades a tatuadores")
public class TatuadorEspecialidadController {

    private final UserService userService;

    @GetMapping("/{userId}/especialidades")
    @PreAuthorize(Permisos.AUTENTICADO)
    @Operation(summary = "Obtener especialidades de un tatuador")
    public ResponseEntity<ApiResponse<List<EspecialidadDto.Response>>> getEspecialidades(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.ok(userService.findEspecialidades(userId)));
    }

    @PostMapping("/{userId}/asignar-especialidades")
    @PreAuthorize(Permisos.ADMINISTRADOR)
    @Operation(summary = "Asignar especialidades a un tatuador")
    public ResponseEntity<ApiResponse<Void>> asignarEspecialidades(
            @PathVariable Long userId,
            @RequestBody List<Long> especialidadIds) {
        userService.assignEspecialidades(userId, especialidadIds);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @DeleteMapping("/{userId}/quitar-especialidad/{espId}")
    @PreAuthorize(Permisos.ADMINISTRADOR)
    @Operation(summary = "Quitar una especialidad de un tatuador")
    public ResponseEntity<ApiResponse<Void>> quitarEspecialidad(
            @PathVariable Long userId,
            @PathVariable Long espId) {
        userService.removeEspecialidad(userId, espId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
