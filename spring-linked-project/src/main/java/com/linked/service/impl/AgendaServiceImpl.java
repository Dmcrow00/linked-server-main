package com.linked.service.impl;

import com.linked.dto.HorarioHabitualDto;
import com.linked.dto.SlotDto;
import com.linked.mapper.HorarioHabitualMapper;
import com.linked.model.entity.HorarioHabitual;
import com.linked.model.entity.TurnoTatuador;
import com.linked.repository.HorarioHabitualRepository;
import com.linked.repository.SweUsrRepository;
import com.linked.repository.TurnoTatuadorRepository;
import com.linked.service.AgendaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Implementación de {@link AgendaService}. */
@Service
@RequiredArgsConstructor
public class AgendaServiceImpl implements AgendaService {

    private final HorarioHabitualRepository horarioRepository;
    private final TurnoTatuadorRepository turnoTatuadorRepository;
    private final SweUsrRepository sweUsrRepository;
    private final HorarioHabitualMapper horarioMapper;

    @Override
    @Transactional(readOnly = true)
    public SlotDto getSlotsDisponibles(Long tatuadorId, LocalDate fecha) {
        // dia_semana usa la convención de JavaScript (domingo = 0 … sábado = 6) y
        // DayOfWeek de Java va de lunes = 1 a domingo = 7: el módulo 7 los hace coincidir.
        int diaSemanaJs = fecha.getDayOfWeek().getValue() % 7;

        HorarioHabitual horario = horarioRepository
                .findByTatuador_IdAndDiaSemana(tatuadorId, diaSemanaJs)
                .orElse(null);

        if (horario == null) {
            return new SlotDto(List.of());
        }

        LocalDateTime inicio = fecha.atTime(horario.getHoraInicio());
        LocalDateTime fin = fecha.atTime(horario.getHoraFin());

        List<TurnoTatuador> reservados = turnoTatuadorRepository
                .findReservadosEnRango(tatuadorId, inicio, fin);

        Set<String> ocupados = new HashSet<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm");
        for (TurnoTatuador tt : reservados) {
            ocupados.add(tt.getTurnoSesion().getFechaHoraInicio().format(fmt));
        }

        // Se recorre la franja en pasos de la duración del turno; el último turno debe
        // terminar dentro del horario de atención.
        List<String> slots = new ArrayList<>();
        LocalTime cursor = horario.getHoraInicio();
        LocalTime limite = horario.getHoraFin();
        int duracion = horario.getDuracionTurnoMin();

        while (!cursor.isAfter(limite.minusMinutes(duracion))) {
            String slot = cursor.format(fmt);
            if (!ocupados.contains(slot)) {
                slots.add(slot);
            }
            cursor = cursor.plusMinutes(duracion);
        }

        return new SlotDto(slots);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getFechasBloqueadas(Long tatuadorId, LocalDate desde, LocalDate hasta) {
        List<String> bloqueadas = new ArrayList<>();
        LocalDate cursor = desde;

        while (!cursor.isAfter(hasta)) {
            SlotDto slots = getSlotsDisponibles(tatuadorId, cursor);
            if (slots.slots().isEmpty()) {
                bloqueadas.add(cursor.format(DateTimeFormatter.ISO_LOCAL_DATE));
            }
            cursor = cursor.plusDays(1);
        }
        return bloqueadas;
    }

    @Override
    @Transactional(readOnly = true)
    public List<HorarioHabitualDto.Response> getHorarioHabitual(Long tatuadorId) {
        return horarioMapper.toResponseList(
                horarioRepository.findByTatuador_IdOrderByDiaSemanaAsc(tatuadorId));
    }

    @Override
    @Transactional
    public void saveHorarioHabitual(Long tatuadorId, HorarioHabitualDto.SaveRequest request) {
        horarioRepository.deleteByTatuador_Id(tatuadorId);
        // Hibernate ejecuta los INSERT antes que los DELETE pendientes: sin este flush, volver a
        // guardar un día ya existente viola la restricción única (tatuador, dia_semana).
        horarioRepository.flush();

        for (HorarioHabitualDto.DiaHorario dh : request.horarios()) {
            HorarioHabitual h = new HorarioHabitual();
            h.setTatuador(sweUsrRepository.getReferenceById(tatuadorId));
            h.setDiaSemana(dh.diaSemana());
            h.setHoraInicio(LocalTime.parse(dh.horaInicio()));
            h.setHoraFin(LocalTime.parse(dh.horaFin()));
            h.setDuracionTurnoMin(dh.duracionTurnoMin());
            horarioRepository.save(h);
        }
    }
}
