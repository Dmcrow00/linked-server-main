package com.linked.repository;

import com.linked.model.entity.SweRolapl;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/** Acceso a roles ({@link SweRolapl}). */
public interface SweRolRepository extends JpaRepository<SweRolapl, Long> {

    Optional<SweRolapl> findByDescription(String description);

    List<SweRolapl> findAllByStatus(String status);
}
