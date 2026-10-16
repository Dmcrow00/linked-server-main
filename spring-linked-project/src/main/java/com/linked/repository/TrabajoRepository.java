package com.linked.repository;

import com.linked.model.entity.Trabajo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/** Acceso a trabajos del portfolio ({@link Trabajo}). */
public interface TrabajoRepository extends JpaRepository<Trabajo, Long> {

    List<Trabajo> findByTatuador_IdAndStatusOrderByCreationTimestampDesc(Long tatuadorId, String status);

    /** Trabajos activos del más reciente al más antiguo; paginado para el feed. */
    @Query("SELECT t FROM Trabajo t WHERE t.status = 'true' ORDER BY t.creationTimestamp DESC")
    Page<Trabajo> findAllActive(Pageable pageable);

    /** Trabajos importados de Instagram que siguen publicados; la sincronización da de baja los borrados allá. */
    List<Trabajo> findByTatuador_IdAndInstagramMediaIdIsNotNullAndStatus(Long tatuadorId, String status);

    /** Lo usa la sincronización con Instagram para no importar dos veces el mismo post. */
    boolean existsByInstagramMediaId(String instagramMediaId);
}
