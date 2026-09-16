package com.linked.repository;

import com.linked.model.entity.Especialidad;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** Acceso a especialidades ({@link Especialidad}). */
public interface EspecialidadRepository extends JpaRepository<Especialidad, Long> {

    List<Especialidad> findAllByStatusOrderByNombreAsc(String status);

    boolean existsByNombreIgnoreCase(String nombre);

    /** Valida nombre único al editar, excluyendo a la propia especialidad. */
    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);
}
