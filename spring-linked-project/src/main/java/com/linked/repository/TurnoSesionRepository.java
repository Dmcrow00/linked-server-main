package com.linked.repository;

import com.linked.model.entity.TurnoSesion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Acceso a turnos ({@link TurnoSesion}).
 * <p>
 * Cada consulta trae por JOIN FETCH una sola colección {@code List}: Hibernate no permite
 * cargar dos a la vez ({@code MultipleBagFetchException}). Las imágenes y los mensajes se
 * cargan de forma diferida dentro de la transacción del servicio.
 */
public interface TurnoSesionRepository extends JpaRepository<TurnoSesion, Long> {

    /** Turnos de un cliente con cliente y tatuadores ya cargados, del más reciente al más antiguo. */
    @Query("SELECT DISTINCT ts FROM TurnoSesion ts " +
           "LEFT JOIN FETCH ts.cliente " +
           "LEFT JOIN FETCH ts.turnoTatuadores tt " +
           "LEFT JOIN FETCH tt.tatuador " +
           "WHERE ts.cliente.id = :clienteId " +
           "ORDER BY ts.fechaHoraInicio DESC")
    List<TurnoSesion> findByClienteIdWithDetails(@Param("clienteId") Long clienteId);

    /** Turnos asignados a un tatuador con cliente y tatuadores ya cargados, del más reciente al más antiguo. */
    @Query("SELECT DISTINCT ts FROM TurnoSesion ts " +
           "LEFT JOIN FETCH ts.cliente " +
           "LEFT JOIN FETCH ts.turnoTatuadores tt " +
           "LEFT JOIN FETCH tt.tatuador " +
           "WHERE tt.tatuador.id = :tatuadorId " +
           "ORDER BY ts.fechaHoraInicio DESC")
    List<TurnoSesion> findByTatuadorIdWithDetails(@Param("tatuadorId") Long tatuadorId);

    /** Un turno con cliente y tatuadores ya cargados. */
    @Query("SELECT ts FROM TurnoSesion ts " +
           "LEFT JOIN FETCH ts.cliente " +
           "LEFT JOIN FETCH ts.turnoTatuadores tt " +
           "LEFT JOIN FETCH tt.tatuador " +
           "WHERE ts.id = :id")
    Optional<TurnoSesion> findByIdWithDetails(@Param("id") Long id);
}
