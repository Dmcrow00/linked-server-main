package com.linked.repository;

import com.linked.model.entity.TurnoTatuador;
import com.linked.model.entity.TurnoTatuadorId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

/** Acceso a las asignaciones tatuador-turno ({@link TurnoTatuador}). */
public interface TurnoTatuadorRepository extends JpaRepository<TurnoTatuador, TurnoTatuadorId> {

    /**
     * Turnos del tatuador que empiezan dentro del rango y ocupan agenda: pendientes, confirmados
     * y propuestos. Se usa para calcular los horarios libres de un día.
     */
    @Query("SELECT tt FROM TurnoTatuador tt " +
           "JOIN FETCH tt.turnoSesion ts " +
           "WHERE tt.id.idTatuador = :tatuadorId " +
           "AND ts.fechaHoraInicio BETWEEN :inicio AND :fin " +
           "AND ts.estado IN ('Pendiente', 'Confirmada', 'Propuesta')")
    List<TurnoTatuador> findReservadosEnRango(
            @Param("tatuadorId") Long tatuadorId,
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin);

    /**
     * Indica si el tatuador tiene algún turno que se superpone con el rango
     * (empieza antes de {@code fin} y termina después de {@code inicio}).
     * Los turnos cancelados o rechazados no cuentan.
     */
    @Query("SELECT COUNT(tt) > 0 FROM TurnoTatuador tt " +
           "JOIN tt.turnoSesion ts " +
           "WHERE tt.id.idTatuador = :tatuadorId " +
           "AND ts.estado NOT IN ('Cancelada', 'Rechazada') " +
           "AND ts.fechaHoraInicio < :fin " +
           "AND ts.fechaHoraFin > :inicio")
    boolean existsTurnoSolapado(
            @Param("tatuadorId") Long tatuadorId,
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin);

    /** Indica si el tatuador está asignado al turno; se usa para validar permisos. */
    boolean existsByTatuador_IdAndTurnoSesion_Id(Long tatuadorId, Long turnoSesionId);
}
