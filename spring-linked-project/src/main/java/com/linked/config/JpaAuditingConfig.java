package com.linked.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * Completa automáticamente {@code creationUser} y {@code modificationUser} de las entidades.
 * <p>
 * Usa el username autenticado, o {@code "SYSTEM"} cuando la operación no tiene sesión
 * (registro, tareas programadas). Se trunca a 25 caracteres porque ese es el largo de
 * las columnas de auditoría.
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class JpaAuditingConfig {

    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()
                    || "anonymousUser".equals(auth.getPrincipal())) {
                return Optional.of("SYSTEM");
            }
            String name = auth.getName();
            return Optional.of(name.length() > 25 ? name.substring(0, 25) : name);
        };
    }
}
