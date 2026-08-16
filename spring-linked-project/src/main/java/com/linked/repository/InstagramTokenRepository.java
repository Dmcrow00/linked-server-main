package com.linked.repository;

import com.linked.model.entity.InstagramToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface InstagramTokenRepository extends JpaRepository<InstagramToken, Long> {

    Optional<InstagramToken> findByTatuador_Id(Long tatuadorId);

    void deleteByTatuador_Id(Long tatuadorId);

    @Query("SELECT t FROM InstagramToken t WHERE t.tokenExpiresAt < :threshold")
    List<InstagramToken> findExpiringBefore(@Param("threshold") LocalDateTime threshold);
}
