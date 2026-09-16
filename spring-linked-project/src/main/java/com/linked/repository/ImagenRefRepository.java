package com.linked.repository;

import com.linked.model.entity.ImagenRef;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** Acceso a imágenes de referencia de turnos ({@link ImagenRef}). */
public interface ImagenRefRepository extends JpaRepository<ImagenRef, Long> {

    List<ImagenRef> findByTurnoSesion_Id(Long turnoSesionId);
}
