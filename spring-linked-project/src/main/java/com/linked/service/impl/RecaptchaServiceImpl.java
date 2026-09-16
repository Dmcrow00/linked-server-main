package com.linked.service.impl;

import com.linked.exception.AppExceptions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

/**
 * Validación de tokens de Google reCAPTCHA.
 * Se activa con {@code app.recaptcha.enabled}, que está habilitado en el perfil {@code prod}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecaptchaServiceImpl {

    private static final String VERIFY_URL = "https://www.google.com/recaptcha/api/siteverify";

    private final WebClient.Builder webClientBuilder;

    @Value("${app.recaptcha.secret-key:}")
    private String secretKey;

    @Value("${app.recaptcha.enabled:false}")
    private boolean enabled;

    /** No hace nada si reCAPTCHA está deshabilitado; si está habilitado exige un token válido. */
    public void verifyIfEnabled(String token) {
        if (!enabled) return;
        if (token == null || token.isBlank()) {
            throw new AppExceptions.BadRequestException("Token reCAPTCHA requerido");
        }
        if (!verify(token)) {
            throw new AppExceptions.BadRequestException("reCAPTCHA inválido");
        }
    }

    @SuppressWarnings("unchecked")
    private boolean verify(String token) {
        try {
            Map<?, ?> response = webClientBuilder.build()
                    .post()
                    .uri(VERIFY_URL + "?secret={secret}&response={response}", secretKey, token)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            return response != null && Boolean.TRUE.equals(response.get("success"));
        } catch (Exception e) {
            log.error("ReCaptcha verification failed: {}", e.getMessage());
            return false;
        }
    }
}
