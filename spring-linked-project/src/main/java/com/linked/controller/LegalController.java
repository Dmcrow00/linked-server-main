package com.linked.controller;

import com.linked.security.Permisos;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

/** Páginas legales públicas, en HTML generado con Thymeleaf ({@code templates/legal}). */
@RestController
@RequestMapping("/api/legal")
@RequiredArgsConstructor
@Tag(name = "Legal", description = "Páginas legales y política de privacidad")
public class LegalController {

    private final TemplateEngine templateEngine;

    @GetMapping("/privacidad")
    @PreAuthorize(Permisos.PUBLICO)
    @Operation(summary = "Política de privacidad")
    public ResponseEntity<String> privacidad() {
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(templateEngine.process("legal/privacidad", new Context()));
    }

    @GetMapping("/terminos")
    @PreAuthorize(Permisos.PUBLICO)
    @Operation(summary = "Términos y condiciones")
    public ResponseEntity<String> terminos() {
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(templateEngine.process("legal/terminos", new Context()));
    }

    @GetMapping("/datadeletion")
    @PreAuthorize(Permisos.PUBLICO)
    @Operation(summary = "Política de eliminación de datos")
    public ResponseEntity<String> dataDeletion() {
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(templateEngine.process("legal/datadeletion", new Context()));
    }
}
