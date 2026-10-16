package com.linked.repository;

import com.linked.model.entity.InstagramToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

/** Acceso a tokens de Instagram ({@link InstagramToken}). */
public interface InstagramTokenRepository extends JpaRepository<InstagramToken, Long> {

    Optional<InstagramToken> findByTatuador_Id(Long tatuadorId);

    void deleteByTatuador_Id(Long tatuadorId);

    /** Tatuadores con cuenta vinculada; los recorre la sincronización programada. */
    @Query("SELECT t.tatuador.id FROM InstagramToken t ORDER BY t.tatuador.id")
    List<Long> findIdsDeTatuadores();
}
