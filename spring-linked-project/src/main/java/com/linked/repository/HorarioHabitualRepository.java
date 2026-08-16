package com.linked.repository;

import com.linked.model.entity.HorarioHabitual;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HorarioHabitualRepository extends JpaRepository<HorarioHabitual, Long> {

    List<HorarioHabitual> findByTatuador_IdOrderByDiaSemanaAsc(Long tatuadorId);

    Optional<HorarioHabitual> findByTatuador_IdAndDiaSemana(Long tatuadorId, int diaSemana);

    void deleteByTatuador_Id(Long tatuadorId);
}
