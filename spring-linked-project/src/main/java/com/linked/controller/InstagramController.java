package com.linked.controller;

import com.linked.dto.ApiResponse;
import com.linked.dto.InstagramStatusDto;
import com.linked.model.entity.SweUsrapl;
import com.linked.service.InstagramService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

/**
 * Vinculación de la cuenta de Instagram de un tatuador y sincronización de sus posts.
 * <p>
 * El callback es público porque lo invoca Instagram, y siempre termina redirigiendo al perfil
 * del cliente web con el resultado en la query, que es lo que esa pantalla sabe interpretar.
 */
@Slf4j
@RestController
@RequestMapping("/api/instagram")
@RequiredArgsConstructor
@PreAuthorize("hasRole('Tatuador')")
@Tag(name = "Instagram", description = "Integración OAuth con Instagram")
public class InstagramController {

    /** Pantalla de perfil del cliente web, donde se muestra el resultado de la vinculación. */
    private static final String RUTA_PERFIL = "/info";

    private final InstagramService instagramService;

    @Value("${app.frontend.base-url:http://localhost:4200}")
    private String frontendBaseUrl;

    /**
     * El navegador llega por redirección y no por fetch, así que no puede mandar el header
     * Authorization: el JWT viaja como parámetro {@code token} y lo resuelve
     * {@code JwtAuthenticationFilter}.
     */
    @GetMapping("/auth")
    @Operation(summary = "Iniciar flujo OAuth de Instagram")
    public ResponseEntity<Void> initiateOAuth(@AuthenticationPrincipal SweUsrapl currentUser) {
        String authUrl = instagramService.initiateOAuth(currentUser.getId());
        return ResponseEntity.status(302).location(URI.create(authUrl)).build();
    }

    @GetMapping("/callback")
    @PreAuthorize("permitAll()")
    @Operation(summary = "Callback OAuth de Instagram (público)")
    public ResponseEntity<Void> callback(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String error,
            @RequestParam(name = "error_reason", required = false) String errorReason) {

        // Instagram vuelve sin código cuando el usuario cancela la autorización.
        if (error != null || code == null || state == null) {
            log.info("Vinculación de Instagram no completada: error={}, reason={}", error, errorReason);
            return redirigirAlPerfil("denied");
        }

        try {
            instagramService.handleCallback(code, state);
            return redirigirAlPerfil("success");
        } catch (Exception ex) {
            // Un 500 dejaría al usuario parado en una pantalla del backend; se vuelve al perfil.
            log.error("Error vinculando la cuenta de Instagram: {}", ex.getMessage());
            return redirigirAlPerfil("error");
        }
    }

    @PostMapping("/sync")
    @Operation(summary = "Sincronizar posts de Instagram")
    public ResponseEntity<ApiResponse<Void>> sync(@AuthenticationPrincipal SweUsrapl currentUser) {
        instagramService.syncPosts(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @GetMapping("/status")
    @Operation(summary = "Estado de conexión con Instagram")
    public ResponseEntity<ApiResponse<InstagramStatusDto>> getStatus(
            @AuthenticationPrincipal SweUsrapl currentUser) {
        return ResponseEntity.ok(ApiResponse.ok(instagramService.getStatus(currentUser.getId())));
    }

    @DeleteMapping("/disconnect")
    @Operation(summary = "Desconectar Instagram")
    public ResponseEntity<ApiResponse<Void>> disconnect(@AuthenticationPrincipal SweUsrapl currentUser) {
        instagramService.disconnect(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    private ResponseEntity<Void> redirigirAlPerfil(String resultado) {
        String url = UriComponentsBuilder.fromUriString(frontendBaseUrl + RUTA_PERFIL)
                .queryParam("instagram", resultado)
                .toUriString();
        return ResponseEntity.status(302).location(URI.create(url)).build();
    }
}
