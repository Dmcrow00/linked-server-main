package com.linked.repository;

import com.linked.model.entity.TrabajoFoto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** Acceso a fotos de trabajos ({@link TrabajoFoto}). */
public interface TrabajoFotoRepository extends JpaRepository<TrabajoFoto, Long> {

    List<TrabajoFoto> findByTrabajo_Id(Long trabajoId);

    int countByTrabajo_Id(Long trabajoId);
}
