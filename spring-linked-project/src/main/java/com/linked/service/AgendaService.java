package com.linked.service;

import com.linked.dto.HorarioHabitualDto;
import com.linked.dto.SlotDto;

import java.time.LocalDate;
import java.util.List;

/** Agenda de los tatuadores: horario habitual semanal y horarios disponibles para reservar. */
public interface AgendaService {

    /**
     * Horarios libres de un día: divide la franja habitual del tatuador en turnos de la duración
     * configurada y descarta los que ya tienen un turno pendiente, confirmado o propuesto.
     */
    SlotDto getSlotsDisponibles(Long tatuadorId, LocalDate fecha);

    /** Días del rango (ambos extremos incluidos) en los que el tatuador no tiene horarios libres. */
    List<String> getFechasBloqueadas(Long tatuadorId, LocalDate desde, LocalDate hasta);

    List<HorarioHabitualDto.Response> getHorarioHabitual(Long tatuadorId);

    /** Reemplaza el horario habitual completo del tatuador por el recibido. */
    void saveHorarioHabitual(Long tatuadorId, HorarioHabitualDto.SaveRequest request);
}
