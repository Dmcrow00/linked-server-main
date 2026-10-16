package com.linked.controller;

import com.dmc00.core.swe.model.entity.SweUsrapl;
import com.dmc00.core.web.ApiResponse;
import com.linked.dto.InstagramStatusDto;
import com.linked.dto.SincronizacionInstagramDto;
import com.linked.security.Permisos;
import com.linked.service.InstagramService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

/**
 * Vinculación de la cuenta de Instagram de un tatuador y sincronización de sus publicaciones.
 * <p>
 * El callback es público porque lo invoca Instagram, y siempre termina redirigiendo al perfil
 * del cliente web con el resultado en la query, que es lo que esa pantalla sabe interpretar.
 */
@Slf4j
@RestController
@RequestMapping("/api/instagram")
@Tag(name = "Instagram", description = "Integración OAuth con Instagram")
public class InstagramController {

    /** Pantalla de perfil del cliente web, donde se muestra el resultado de la vinculación. */
    static final String RUTA_PERFIL = "/info";

    private final InstagramService instagramService;
    private final String urlDelCliente;

    public InstagramController(InstagramService instagramService,
                               @Value("${app.frontend.base-url:http://localhost:4200}") String urlDelCliente) {
        this.instagramService = instagramService;
        this.urlDelCliente = urlDelCliente;
    }

    /**
     * El navegador llega por redirección y no por fetch, así que no puede mandar el header
     * Authorization: el JWT viaja como parámetro {@code token} y lo resuelve
     * {@code JwtConTokenEnInicioDeOAuth}.
     */
    @GetMapping("/auth")
    @PreAuthorize(Permisos.TATUADOR)
    @Operation(summary = "Iniciar flujo OAuth de Instagram")
    public ResponseEntity<Void> initiateOAuth(@AuthenticationPrincipal SweUsrapl usuario) {
        return ResponseEntity.status(302).location(URI.create(instagramService.initiateOAuth(usuario.getId()))).build();
    }

    @GetMapping("/callback")
    @PreAuthorize(Permisos.PUBLICO)
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
        } catch (RuntimeException ex) {
            // Un error dejaría al usuario parado en una pantalla del backend; se vuelve al perfil.
            log.error("Error vinculando la cuenta de Instagram: {}", ex.getMessage());
            return redirigirAlPerfil("error");
        }
    }

    @PostMapping("/sync")
    @PreAuthorize(Permisos.TATUADOR)
    @Operation(summary = "Sincronizar publicaciones de Instagram")
    public ResponseEntity<ApiResponse<SincronizacionInstagramDto>> sync(@AuthenticationPrincipal SweUsrapl usuario) {
        SincronizacionInstagramDto resultado = instagramService.syncPosts(usuario.getId());
        return ResponseEntity.ok(ApiResponse.ok(resultado, "Sincronización completada: " + resultado.nuevos()
                + " nuevos, " + resultado.eliminados() + " eliminados, " + resultado.omitidos() + " omitidos."));
    }

    @GetMapping("/status")
    @PreAuthorize(Permisos.TATUADOR)
    @Operation(summary = "Estado de conexión con Instagram")
    public ResponseEntity<ApiResponse<InstagramStatusDto>> getStatus(@AuthenticationPrincipal SweUsrapl usuario) {
        return ResponseEntity.ok(ApiResponse.ok(instagramService.getStatus(usuario.getId())));
    }

    @DeleteMapping("/disconnect")
    @PreAuthorize(Permisos.TATUADOR)
    @Operation(summary = "Desconectar Instagram")
    public ResponseEntity<ApiResponse<Void>> disconnect(@AuthenticationPrincipal SweUsrapl usuario) {
        instagramService.disconnect(usuario.getId());
        return ResponseEntity.ok(ApiResponse.ok(null, "Cuenta de Instagram desvinculada correctamente."));
    }

    private ResponseEntity<Void> redirigirAlPerfil(String resultado) {
        String url = UriComponentsBuilder.fromUriString(urlDelCliente + RUTA_PERFIL)
                .queryParam("instagram", resultado)
                .toUriString();
        return ResponseEntity.status(302).location(URI.create(url)).build();
    }
}
