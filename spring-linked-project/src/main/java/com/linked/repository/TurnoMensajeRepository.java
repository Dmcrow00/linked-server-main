package com.linked.repository;

import com.linked.model.entity.TurnoMensaje;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TurnoMensajeRepository extends JpaRepository<TurnoMensaje, Long> {

    List<TurnoMensaje> findByTurnoSesion_IdOrderByCreationTimestampAsc(Long turnoSesionId);

    int countByTurnoSesion_IdAndLeidoFalseAndUsuarioEnvia_IdNot(Long turnoSesionId, Long userId);
}
