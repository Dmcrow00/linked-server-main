package com.linked.config;

import com.dmc00.core.security.JwtAuthenticationFilter;
import com.dmc00.core.security.JwtService;
import com.dmc00.core.security.SeguridadHttpComun;
import com.linked.security.JwtConTokenEnInicioDeOAuth;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Seguridad HTTP de la API. Lo común (sin sesión, CORS, JWT, 401/403 en JSON, contraseñas) lo
 * pone core-security; acá solo se separa lo público de lo que exige sesión. El permiso fino de
 * cada endpoint está en su {@code @PreAuthorize} ({@link com.linked.security.Permisos}).
 */
@Configuration
public class SecurityConfig {

    /** El filtro de core-security, con el token por query solo en el inicio del OAuth de Instagram. */
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtService jwtService, UserDetailsService usuarios) {
        return new JwtConTokenEnInicioDeOAuth(jwtService, usuarios);
    }

    @Bean
    public SecurityFilterChain seguridad(HttpSecurity http, SeguridadHttpComun comun) throws Exception {
        comun.aplicar(http).authorizeHttpRequests(auth -> auth
                // ── Login, clave pública y disponibilidad (core-swe) ──────────
                .requestMatchers("/api/auth/**").permitAll()
                // ── Páginas legales, callback de Instagram y operación ────────
                .requestMatchers("/api/legal/**").permitAll()
                .requestMatchers("/api/instagram/callback").permitAll()
                .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/api-docs/**").permitAll()
                .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
                // ── Registro y recuperación de contraseña ─────────────────────
                .requestMatchers(HttpMethod.POST,
                        "/api/users/register", "/api/users/register-tatuador",
                        "/api/users/forgot-password", "/api/users/reset-password").permitAll()
                // ── Feed, portfolio, noticias, especialidades y búsqueda ──────
                .requestMatchers(HttpMethod.GET,
                        "/api/trabajos/feed", "/api/trabajos/tatuador/*", "/api/users/*/publico",
                        "/api/news", "/api/news/**", "/api/especialidades", "/api/especialidades/**",
                        "/api/busqueda/tatuadores-ubicacion").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/busqueda/por-estilo").permitAll()
                // ── Soporte: se puede reportar un problema sin iniciar sesión ──
                .requestMatchers(HttpMethod.POST, "/api/supportTicket/create").permitAll()
                // ── El resto exige sesión; el rol lo decide cada @PreAuthorize ─
                .anyRequest().authenticated());
        return http.build();
    }
}
