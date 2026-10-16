package com.linked.controller;

import com.dmc00.core.swe.model.entity.SweUsrapl;
import com.dmc00.core.web.ApiResponse;
import com.linked.dto.TrabajoDto;
import com.linked.security.Permisos;
import com.linked.service.TrabajosService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
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
    @PreAuthorize(Permisos.PUBLICO)
    @Operation(summary = "Feed público de trabajos")
    public ResponseEntity<ApiResponse<List<TrabajoDto.Response>>> getFeed(@AuthenticationPrincipal SweUsrapl usuario) {
        return ResponseEntity.ok(ApiResponse.ok(trabajosService.getFeed(idDe(usuario))));
    }

    @GetMapping("/tatuador/{id}")
    @PreAuthorize(Permisos.PUBLICO)
    @Operation(summary = "Trabajos de un tatuador")
    public ResponseEntity<ApiResponse<List<TrabajoDto.Response>>> getByTatuador(
            @PathVariable Long id, @AuthenticationPrincipal SweUsrapl usuario) {
        return ResponseEntity.ok(ApiResponse.ok(trabajosService.getByTatuador(id, idDe(usuario))));
    }

    @GetMapping("/{id}")
    @PreAuthorize(Permisos.AUTENTICADO)
    @Operation(summary = "Obtener trabajo por ID")
    public ResponseEntity<ApiResponse<TrabajoDto.Response>> getById(
            @PathVariable Long id, @AuthenticationPrincipal SweUsrapl usuario) {
        return ResponseEntity.ok(ApiResponse.ok(trabajosService.getById(id, usuario.getId())));
    }

    /** El trabajo siempre se publica a nombre del tatuador autenticado. */
    @PostMapping(consumes = "multipart/form-data")
    @PreAuthorize(Permisos.TATUADOR)
    @Operation(summary = "Subir nuevo trabajo (max 5 fotos)")
    public ResponseEntity<ApiResponse<TrabajoDto.Response>> subirTrabajo(
            @AuthenticationPrincipal SweUsrapl usuario,
            @RequestParam(required = false) String descripcion,
            @RequestParam("fotos") MultipartFile[] fotos) {
        TrabajoDto.Response response = trabajosService.subirTrabajo(
                usuario.getId(), new TrabajoDto.CreateRequest(descripcion), fotos);
        return ResponseEntity.created(URI.create("/api/trabajos/" + response.id()))
                .body(ApiResponse.created(response));
    }

    @PostMapping("/{id}/favorito")
    @PreAuthorize(Permisos.CLIENTE)
    @Operation(summary = "Dar like a un trabajo")
    public ResponseEntity<ApiResponse<Void>> darLike(@PathVariable Long id, @AuthenticationPrincipal SweUsrapl usuario) {
        trabajosService.darLike(usuario.getId(), id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @DeleteMapping("/{id}/favorito")
    @PreAuthorize(Permisos.CLIENTE)
    @Operation(summary = "Quitar like de un trabajo")
    public ResponseEntity<ApiResponse<Void>> quitarLike(@PathVariable Long id, @AuthenticationPrincipal SweUsrapl usuario) {
        trabajosService.quitarLike(usuario.getId(), id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @GetMapping("/mis-favoritos/ids")
    @PreAuthorize(Permisos.AUTENTICADO)
    @Operation(summary = "IDs de trabajos que me gustan")
    public ResponseEntity<ApiResponse<List<Long>>> getMisLikesIds(@AuthenticationPrincipal SweUsrapl usuario) {
        return ResponseEntity.ok(ApiResponse.ok(trabajosService.getMisLikesIds(usuario.getId())));
    }

    /** En los endpoints públicos la request puede llegar sin sesión. */
    private static Long idDe(SweUsrapl usuario) {
        return usuario != null ? usuario.getId() : null;
    }
}
