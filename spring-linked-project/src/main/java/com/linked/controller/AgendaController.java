package com.linked.controller;

import com.dmc00.core.swe.model.entity.SweUsrapl;
import com.dmc00.core.web.ApiResponse;
import com.linked.dto.HorarioHabitualDto;
import com.linked.dto.SlotDto;
import com.linked.security.Permisos;
import com.linked.service.AgendaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/** Disponibilidad de los tatuadores para reservar y configuración de su horario habitual. */
@RestController
@RequestMapping("/api/agenda")
@RequiredArgsConstructor
@Tag(name = "Agenda", description = "Horarios y slots de disponibilidad")
public class AgendaController {

    private final AgendaService agendaService;

    @GetMapping("/{tatuadorId}/slots-dia")
    @PreAuthorize(Permisos.AUTENTICADO)
    @Operation(summary = "Slots disponibles en un día")
    public ResponseEntity<ApiResponse<SlotDto>> getSlotsDisponibles(
            @PathVariable Long tatuadorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return ResponseEntity.ok(ApiResponse.ok(agendaService.getSlotsDisponibles(tatuadorId, fecha)));
    }

    @GetMapping("/{tatuadorId}/fechas-bloqueadas")
    @PreAuthorize(Permisos.AUTENTICADO)
    @Operation(summary = "Fechas sin disponibilidad en un rango")
    public ResponseEntity<ApiResponse<List<String>>> getFechasBloqueadas(
            @PathVariable Long tatuadorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return ResponseEntity.ok(ApiResponse.ok(
                agendaService.getFechasBloqueadas(tatuadorId, desde, hasta)));
    }

    @GetMapping("/horario-habitual")
    @PreAuthorize(Permisos.TATUADOR)
    @Operation(summary = "Ver mi horario habitual (tatuador)")
    public ResponseEntity<ApiResponse<List<HorarioHabitualDto.Response>>> getHorarioHabitual(
            @AuthenticationPrincipal SweUsrapl currentUser) {
        return ResponseEntity.ok(ApiResponse.ok(
                agendaService.getHorarioHabitual(currentUser.getId())));
    }

    @PutMapping("/horario-habitual")
    @PreAuthorize(Permisos.TATUADOR)
    @Operation(summary = "Guardar horario habitual (tatuador)")
    public ResponseEntity<ApiResponse<Void>> saveHorarioHabitual(
            @AuthenticationPrincipal SweUsrapl currentUser,
            @Valid @RequestBody HorarioHabitualDto.SaveRequest request) {
        agendaService.saveHorarioHabitual(currentUser.getId(), request);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
