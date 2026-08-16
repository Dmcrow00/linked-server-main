package com.linked.repository;

import com.linked.model.entity.Trabajo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TrabajoRepository extends JpaRepository<Trabajo, Long> {

    List<Trabajo> findByTatuador_IdAndStatusOrderByCreationTimestampDesc(Long tatuadorId, String status);

    @Query("SELECT t FROM Trabajo t WHERE t.status = 'true' ORDER BY t.creationTimestamp DESC")
    Page<Trabajo> findAllActive(Pageable pageable);

    Optional<Trabajo> findByInstagramMediaId(String instagramMediaId);

    boolean existsByInstagramMediaId(String instagramMediaId);
}
