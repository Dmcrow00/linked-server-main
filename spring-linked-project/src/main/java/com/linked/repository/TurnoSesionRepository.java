package com.linked.repository;

import com.linked.model.entity.TurnoSesion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TurnoSesionRepository extends JpaRepository<TurnoSesion, Long> {

    @Query("SELECT DISTINCT ts FROM TurnoSesion ts " +
           "LEFT JOIN FETCH ts.turnoTatuadores tt " +
           "LEFT JOIN FETCH tt.tatuador " +
           "LEFT JOIN FETCH ts.imagenesRef " +
           "WHERE ts.cliente.id = :clienteId " +
           "ORDER BY ts.fechaHoraInicio DESC")
    List<TurnoSesion> findByClienteIdWithDetails(@Param("clienteId") Long clienteId);

    @Query("SELECT DISTINCT ts FROM TurnoSesion ts " +
           "LEFT JOIN FETCH ts.turnoTatuadores tt " +
           "LEFT JOIN FETCH tt.tatuador " +
           "LEFT JOIN FETCH ts.imagenesRef " +
           "WHERE tt.tatuador.id = :tatuadorId " +
           "ORDER BY ts.fechaHoraInicio DESC")
    List<TurnoSesion> findByTatuadorIdWithDetails(@Param("tatuadorId") Long tatuadorId);

    @Query("SELECT ts FROM TurnoSesion ts " +
           "LEFT JOIN FETCH ts.turnoTatuadores tt " +
           "LEFT JOIN FETCH tt.tatuador " +
           "LEFT JOIN FETCH ts.imagenesRef " +
           "LEFT JOIN FETCH ts.mensajes " +
           "WHERE ts.id = :id")
    Optional<TurnoSesion> findByIdWithDetails(@Param("id") Long id);
}
