package com.linked.service;

import com.linked.dto.HorarioHabitualDto;
import com.linked.dto.SlotDto;

import java.time.LocalDate;
import java.util.List;

public interface AgendaService {

    SlotDto getSlotsDisponibles(Long tatuadorId, LocalDate fecha);

    List<String> getFechasBloqueadas(Long tatuadorId, LocalDate desde, LocalDate hasta);

    List<HorarioHabitualDto.Response> getHorarioHabitual(Long tatuadorId);

    void saveHorarioHabitual(Long tatuadorId, HorarioHabitualDto.SaveRequest request);
}
