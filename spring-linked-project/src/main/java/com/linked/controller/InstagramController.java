package com.linked.controller;

import com.linked.dto.ApiResponse;
import com.linked.dto.InstagramStatusDto;
import com.linked.model.entity.SweUsrapl;
import com.linked.service.InstagramService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

/**
 * Vinculación de la cuenta de Instagram de un tatuador y sincronización de sus posts.
 * El callback es público porque lo invoca Instagram al terminar la autorización.
 */
@RestController
@RequestMapping("/api/instagram")
@RequiredArgsConstructor
@PreAuthorize("hasRole('Tatuador')")
@Tag(name = "Instagram", description = "Integración OAuth con Instagram")
public class InstagramController {

    private final InstagramService instagramService;

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
            @RequestParam String code,
            @RequestParam String state,
            @org.springframework.beans.factory.annotation.Value("${app.frontend.base-url:http://localhost:4200}")
            String frontendBaseUrl) {
        instagramService.handleCallback(code, state);
        return ResponseEntity.status(302)
                .location(URI.create(frontendBaseUrl + "/instagram/success"))
                .build();
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
}
