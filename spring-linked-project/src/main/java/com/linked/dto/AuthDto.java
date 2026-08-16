package com.linked.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;

public class AuthDto {

    public record LoginRequest(
            @NotBlank @JsonAlias({"username", "email"}) String identifier,
            @NotBlank String password,
            String recaptchaToken
    ) {}

    public record TokenResponse(
            String token,
            String username,
            Long userId
    ) {}

    public record CheckAvailabilityResponse(
            boolean available
    ) {}
}
