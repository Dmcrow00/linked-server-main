package com.linked.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Documentación OpenAPI de la API, navegable en {@code /swagger-ui.html}.
 * En el perfil {@code prod} está deshabilitada (ver {@code application-prod.yml}).
 */
@Configuration
public class OpenApiConfig {

    private static final String BEARER_AUTH = "bearerAuth";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("LINKED API - Tattoo Studio Platform")
                        .version("1.0.0")
                        .description("API REST para gestion de estudios de tatuajes")
                        .contact(new Contact()
                                .name("Equipo LINKED")
                                .email("dev@linked-app.com")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Desarrollo"),
                        new Server().url("https://api.linked-app.com").description("Produccion")))
                .components(new Components()
                        .addSecuritySchemes(BEARER_AUTH, new SecurityScheme()
                                .name(BEARER_AUTH)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Ingrese el token JWT obtenido en /api/auth/login")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH));
    }
}
