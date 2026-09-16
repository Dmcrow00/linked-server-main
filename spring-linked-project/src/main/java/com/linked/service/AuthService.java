package com.linked.service;

import com.linked.dto.AuthDto;

/** Autenticación de usuarios. */
public interface AuthService {

    /**
     * Valida las credenciales (usuario o email y contraseña cifrada con RSA) y emite el JWT.
     * Para los tatuadores, además dispara en segundo plano la sincronización con Instagram.
     */
    AuthDto.TokenResponse login(AuthDto.LoginRequest request);

    /**
     * @param field {@code "username"} o {@code "email"}
     * @return si el valor todavía no está registrado
     */
    AuthDto.CheckAvailabilityResponse checkAvailability(String field, String value);
}
