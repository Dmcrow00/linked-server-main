package com.linked.controller;

import com.linked.dto.ApiResponse;
import com.linked.dto.EspecialidadDto;
import com.linked.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Especialidades asignadas a cada tatuador. */
@RestController
@RequestMapping("/api/tatuador")
@RequiredArgsConstructor
@Tag(name = "Tatuador - Especialidades", description = "Asignación de especialidades a tatuadores")
public class TatuadorEspecialidadController {

    private final UserService userService;

    @GetMapping("/{userId}/especialidades")
    @Operation(summary = "Obtener especialidades de un tatuador")
    public ResponseEntity<ApiResponse<List<EspecialidadDto.Response>>> getEspecialidades(
            @PathVariable Long userId) {
        // Las especialidades ya vienen incluidas en el perfil del usuario.
        return ResponseEntity.ok(ApiResponse.ok(
                userService.findById(userId).especialidades()));
    }

    @PostMapping("/{userId}/asignar-especialidades")
    @PreAuthorize("hasRole('Administrador')")
    @Operation(summary = "Asignar especialidades a un tatuador")
    public ResponseEntity<ApiResponse<Void>> asignarEspecialidades(
            @PathVariable Long userId,
            @RequestBody List<Long> especialidadIds) {
        userService.assignEspecialidades(userId, especialidadIds);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @DeleteMapping("/{userId}/quitar-especialidad/{espId}")
    @PreAuthorize("hasRole('Administrador')")
    @Operation(summary = "Quitar una especialidad de un tatuador")
    public ResponseEntity<ApiResponse<Void>> quitarEspecialidad(
            @PathVariable Long userId,
            @PathVariable Long espId) {
        userService.removeEspecialidad(userId, espId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
