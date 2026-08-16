package com.linked.repository;

import com.linked.model.entity.TurnoTatuador;
import com.linked.model.entity.TurnoTatuadorId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface TurnoTatuadorRepository extends JpaRepository<TurnoTatuador, TurnoTatuadorId> {

    @Query("SELECT tt FROM TurnoTatuador tt " +
           "JOIN FETCH tt.turnoSesion ts " +
           "WHERE tt.id.idTatuador = :tatuadorId " +
           "AND ts.fechaHoraInicio BETWEEN :inicio AND :fin " +
           "AND ts.estado IN ('Pendiente', 'Confirmada', 'Propuesta')")
    List<TurnoTatuador> findReservadosEnRango(
            @Param("tatuadorId") Long tatuadorId,
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin);

    boolean existsByTatuador_IdAndTurnoSesion_Id(Long tatuadorId, Long turnoSesionId);
}
