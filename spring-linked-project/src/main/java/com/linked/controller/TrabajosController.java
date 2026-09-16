package com.linked.controller;

import com.linked.dto.ApiResponse;
import com.linked.dto.TrabajoDto;
import com.linked.model.entity.SweUsrapl;
import com.linked.service.TrabajosService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.List;

/** Portfolio de trabajos de los tatuadores y likes de los clientes. */
@RestController
@RequestMapping("/api/trabajos")
@RequiredArgsConstructor
@Tag(name = "Trabajos", description = "Portfolio de trabajos de tatuadores")
public class TrabajosController {

    private final TrabajosService trabajosService;

    @GetMapping("/feed")
    @Operation(summary = "Feed público de trabajos")
    public ResponseEntity<ApiResponse<List<TrabajoDto.Response>>> getFeed() {
        return ResponseEntity.ok(ApiResponse.ok(trabajosService.getFeed(getCurrentUserId())));
    }

    @GetMapping("/tatuador/{id}")
    @Operation(summary = "Trabajos de un tatuador")
    public ResponseEntity<ApiResponse<List<TrabajoDto.Response>>> getByTatuador(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(trabajosService.getByTatuador(id, getCurrentUserId())));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener trabajo por ID")
    public ResponseEntity<ApiResponse<TrabajoDto.Response>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(trabajosService.getById(id, getCurrentUserId())));
    }

    /** El trabajo siempre se publica a nombre del tatuador autenticado. */
    @PostMapping(consumes = "multipart/form-data")
    @PreAuthorize("hasRole('Tatuador')")
    @Operation(summary = "Subir nuevo trabajo (max 5 fotos)")
    public ResponseEntity<ApiResponse<TrabajoDto.Response>> subirTrabajo(
            @AuthenticationPrincipal SweUsrapl currentUser,
            @RequestParam(required = false) String descripcion,
            @RequestParam("fotos") MultipartFile[] fotos) {
        TrabajoDto.Response response = trabajosService.subirTrabajo(
                currentUser.getId(), new TrabajoDto.CreateRequest(descripcion), fotos);
        return ResponseEntity.created(URI.create("/api/trabajos/" + response.id()))
                .body(ApiResponse.created(response));
    }

    @PostMapping("/{id}/favorito")
    @PreAuthorize("hasRole('Cliente')")
    @Operation(summary = "Dar like a un trabajo")
    public ResponseEntity<ApiResponse<Void>> darLike(@PathVariable Long id) {
        trabajosService.darLike(getCurrentUserId(), id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @DeleteMapping("/{id}/favorito")
    @PreAuthorize("hasRole('Cliente')")
    @Operation(summary = "Quitar like de un trabajo")
    public ResponseEntity<ApiResponse<Void>> quitarLike(@PathVariable Long id) {
        trabajosService.quitarLike(getCurrentUserId(), id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @GetMapping("/mis-favoritos/ids")
    @Operation(summary = "IDs de trabajos que me gustan")
    public ResponseEntity<ApiResponse<List<Long>>> getMisLikesIds() {
        return ResponseEntity.ok(ApiResponse.ok(trabajosService.getMisLikesIds(getCurrentUserId())));
    }

    /** ID del usuario autenticado, o {@code null} si la request llega sin sesión a un endpoint público. */
    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        if (auth.getPrincipal() instanceof SweUsrapl u) {
            return u.getId();
        }
        return null;
    }
}
