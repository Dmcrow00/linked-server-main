package com.linked.repository;

import com.linked.model.entity.TurnoMensaje;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** Acceso a mensajes del chat de turnos ({@link TurnoMensaje}). */
public interface TurnoMensajeRepository extends JpaRepository<TurnoMensaje, Long> {

    /** Mensajes del turno en orden cronológico. */
    List<TurnoMensaje> findByTurnoSesion_IdOrderByCreationTimestampAsc(Long turnoSesionId);

    /** Cantidad de mensajes no leídos del turno enviados por otros usuarios. */
    int countByTurnoSesion_IdAndLeidoFalseAndUsuarioEnvia_IdNot(Long turnoSesionId, Long userId);
}
