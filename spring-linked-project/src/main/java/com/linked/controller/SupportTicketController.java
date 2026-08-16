package com.linked.controller;

import com.linked.dto.ApiResponse;
import com.linked.dto.SupportTicketDto;
import com.linked.model.entity.SweUsrapl;
import com.linked.service.SupportTicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/supportTicket")
@RequiredArgsConstructor
@Tag(name = "Soporte", description = "Gestión de tickets de soporte")
public class SupportTicketController {

    private final SupportTicketService ticketService;

    @GetMapping("/me/tickets")
    @Operation(summary = "Mis tickets de soporte")
    public ResponseEntity<ApiResponse<List<SupportTicketDto.Response>>> myTickets(
            @AuthenticationPrincipal SweUsrapl currentUser) {
        return ResponseEntity.ok(ApiResponse.ok(
                ticketService.findByCurrentUser(currentUser.getUsername())));
    }

    @GetMapping("/findall")
    @PreAuthorize("hasAnyRole('Administrador','Moderador')")
    @Operation(summary = "Listar todos los tickets")
    public ResponseEntity<ApiResponse<List<SupportTicketDto.Response>>> findAll() {
        return ResponseEntity.ok(ApiResponse.ok(ticketService.findAll()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('Administrador','Moderador')")
    @Operation(summary = "Obtener ticket por ID")
    public ResponseEntity<ApiResponse<SupportTicketDto.Response>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(ticketService.findById(id)));
    }

    @PostMapping("/create")
    @Operation(summary = "Crear ticket de soporte")
    public ResponseEntity<ApiResponse<SupportTicketDto.Response>> create(
            @Valid @RequestBody SupportTicketDto.CreateRequest request,
            @AuthenticationPrincipal SweUsrapl currentUser) {
        String username = currentUser != null ? currentUser.getUsername() : null;
        SupportTicketDto.Response response = ticketService.create(request, username);
        return ResponseEntity.created(URI.create("/api/supportTicket/" + response.id()))
                .body(ApiResponse.created(response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('Administrador','Moderador')")
    @Operation(summary = "Actualizar ticket")
    public ResponseEntity<ApiResponse<SupportTicketDto.Response>> update(
            @PathVariable Long id,
            @Valid @RequestBody SupportTicketDto.UpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(ticketService.update(id, request)));
    }

    @PostMapping("/{id}/respond")
    @PreAuthorize("hasAnyRole('Administrador','Moderador')")
    @Operation(summary = "Responder a un ticket")
    public ResponseEntity<ApiResponse<Void>> respond(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        ticketService.respond(id, body.get("adminResponse"));
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('Administrador','Moderador')")
    @Operation(summary = "Eliminar ticket")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        ticketService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
