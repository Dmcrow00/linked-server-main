package com.linked.controller;

import com.linked.dto.ApiResponse;
import com.linked.dto.AuthDto;
import com.linked.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.PublicKey;
import java.util.Base64;
import java.util.Map;

/** Endpoints públicos de autenticación. */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Login y verificación de disponibilidad")
public class AuthController {

    private final AuthService authService;
    private final PublicKey rsaPublicKey;

    /** Clave pública en formato PEM con la que el cliente web cifra las contraseñas. */
    @GetMapping("/public-key")
    @Operation(summary = "Obtener clave pública RSA para cifrado de datos sensibles")
    public ResponseEntity<Map<String, String>> getPublicKey() {
        String base64 = Base64.getMimeEncoder(64, new byte[]{'\n'})
                .encodeToString(rsaPublicKey.getEncoded());
        String pem = "-----BEGIN PUBLIC KEY-----\n" + base64 + "\n-----END PUBLIC KEY-----";
        return ResponseEntity.ok(Map.of("publicKey", pem));
    }

    @GetMapping("/check-availability")
    @Operation(summary = "Verificar disponibilidad de username o email")
    public ResponseEntity<ApiResponse<AuthDto.CheckAvailabilityResponse>> checkAvailability(
            @RequestParam String field,
            @RequestParam String value) {
        return ResponseEntity.ok(ApiResponse.ok(authService.checkAvailability(field, value)));
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión")
    public ResponseEntity<ApiResponse<AuthDto.TokenResponse>> login(
            @Valid @RequestBody AuthDto.LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(authService.login(request)));
    }
}
