package com.linked.repository;

import com.linked.model.entity.ImagenRef;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ImagenRefRepository extends JpaRepository<ImagenRef, Long> {

    List<ImagenRef> findByTurnoSesion_Id(Long turnoSesionId);
}
