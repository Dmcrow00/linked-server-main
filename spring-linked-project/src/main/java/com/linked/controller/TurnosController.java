package com.linked.controller;

import com.linked.dto.ApiResponse;
import com.linked.dto.TurnoMensajeDto;
import com.linked.dto.TurnoSesionDto;
import com.linked.model.entity.SweUsrapl;
import com.linked.service.TurnosService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.List;

/** Turnos: reservas de los clientes, agenda de los tatuadores y chat de cada turno. */
@RestController
@RequestMapping("/api/turnos")
@RequiredArgsConstructor
@Tag(name = "Turnos", description = "Gestión de turnos y mensajería")
public class TurnosController {

    private final TurnosService turnosService;

    @PostMapping(value = "/solicitar", consumes = "multipart/form-data")
    @PreAuthorize("hasRole('Cliente')")
    @Operation(summary = "Solicitar un turno")
    public ResponseEntity<ApiResponse<TurnoSesionDto.Response>> solicitar(
            @AuthenticationPrincipal SweUsrapl currentUser,
            @RequestParam Long tatuadorId,
            @RequestParam String fechaHoraInicio,
            @RequestParam String fechaHoraFin,
            @RequestParam(required = false) String descripcionCliente,
            @RequestParam(value = "imagenes", required = false) MultipartFile[] imagenes) {

        TurnoSesionDto.CreateRequest request = new TurnoSesionDto.CreateRequest(
                tatuadorId, fechaHoraInicio, fechaHoraFin, descripcionCliente);

        TurnoSesionDto.Response response = turnosService.solicitarTurno(
                currentUser.getId(), request, imagenes);
        return ResponseEntity.created(URI.create("/api/turnos/" + response.id()))
                .body(ApiResponse.created(response));
    }

    @GetMapping("/mis-reservas-cliente")
    @Operation(summary = "Mis turnos como cliente")
    public ResponseEntity<ApiResponse<List<TurnoSesionDto.Response>>> misReservasCliente(
            @AuthenticationPrincipal SweUsrapl currentUser) {
        return ResponseEntity.ok(ApiResponse.ok(
                turnosService.getTurnosByCliente(currentUser.getId())));
    }

    @GetMapping("/mis-turnos")
    @PreAuthorize("hasRole('Tatuador')")
    @Operation(summary = "Mis turnos como tatuador")
    public ResponseEntity<ApiResponse<List<TurnoSesionDto.Response>>> misTurnos(
            @AuthenticationPrincipal SweUsrapl currentUser) {
        return ResponseEntity.ok(ApiResponse.ok(
                turnosService.getTurnosByTatuador(currentUser.getId())));
    }

    @PatchMapping("/gestionar/{id}")
    @PreAuthorize("hasRole('Tatuador')")
    @Operation(summary = "Gestionar estado de un turno (tatuador)")
    public ResponseEntity<ApiResponse<TurnoSesionDto.Response>> gestionar(
            @PathVariable Long id,
            @AuthenticationPrincipal SweUsrapl currentUser,
            @Valid @RequestBody TurnoSesionDto.GestionarRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
                turnosService.actualizarEstado(id, request, currentUser.getId())));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar datos de un turno")
    public ResponseEntity<ApiResponse<TurnoSesionDto.Response>> update(
            @PathVariable Long id,
            @AuthenticationPrincipal SweUsrapl currentUser,
            @Valid @RequestBody TurnoSesionDto.UpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
                turnosService.update(id, request, currentUser.getId())));
    }

    @GetMapping("/{id}/mensajes")
    @Operation(summary = "Obtener mensajes de un turno")
    public ResponseEntity<ApiResponse<List<TurnoMensajeDto.Response>>> getMensajes(
            @PathVariable Long id,
            @AuthenticationPrincipal SweUsrapl currentUser) {
        return ResponseEntity.ok(ApiResponse.ok(turnosService.getMensajes(id, currentUser.getId())));
    }

    @PostMapping("/{id}/mensajes")
    @Operation(summary = "Enviar mensaje en un turno")
    public ResponseEntity<ApiResponse<TurnoMensajeDto.Response>> enviarMensaje(
            @PathVariable Long id,
            @AuthenticationPrincipal SweUsrapl currentUser,
            @Valid @RequestBody TurnoMensajeDto.CreateRequest request) {
        TurnoMensajeDto.Response response = turnosService.enviarMensaje(id, currentUser.getId(), request);
        return ResponseEntity.created(URI.create("/api/turnos/" + id + "/mensajes"))
                .body(ApiResponse.created(response));
    }
}
