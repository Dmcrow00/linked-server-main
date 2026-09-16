package com.linked.repository;

import com.linked.model.entity.Trabajo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/** Acceso a trabajos del portfolio ({@link Trabajo}). */
public interface TrabajoRepository extends JpaRepository<Trabajo, Long> {

    List<Trabajo> findByTatuador_IdAndStatusOrderByCreationTimestampDesc(Long tatuadorId, String status);

    /** Trabajos activos del más reciente al más antiguo; paginado para el feed. */
    @Query("SELECT t FROM Trabajo t WHERE t.status = 'true' ORDER BY t.creationTimestamp DESC")
    Page<Trabajo> findAllActive(Pageable pageable);

    Optional<Trabajo> findByInstagramMediaId(String instagramMediaId);

    /** Lo usa la sincronización con Instagram para no importar dos veces el mismo post. */
    boolean existsByInstagramMediaId(String instagramMediaId);
}
