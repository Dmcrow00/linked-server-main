package com.linked.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;

/** Contratos de autenticación ({@code /api/auth}). */
public class AuthDto {

    /**
     * @param identifier username o email; en el JSON también se acepta como "username" o "email"
     * @param password   contraseña cifrada con la clave pública RSA
     */
    public record LoginRequest(
            @NotBlank @JsonAlias({"username", "email"}) String identifier,
            @NotBlank String password,
            String recaptchaToken
    ) {}

    /** Respuesta de login y de registro: el JWT y datos mínimos del usuario. */
    public record TokenResponse(
            String token,
            String username,
            Long userId
    ) {}

    public record CheckAvailabilityResponse(
            boolean available
    ) {}
}
