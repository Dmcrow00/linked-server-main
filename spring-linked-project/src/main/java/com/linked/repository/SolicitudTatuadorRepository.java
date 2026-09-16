package com.linked.repository;

import com.linked.model.entity.SolicitudTatuador;
import com.linked.model.enums.SolicitudStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/** Acceso a solicitudes para ser tatuador ({@link SolicitudTatuador}). */
public interface SolicitudTatuadorRepository extends JpaRepository<SolicitudTatuador, Long> {

    List<SolicitudTatuador> findAllByOrderByCreationTimestampDesc();

    List<SolicitudTatuador> findBySolicitudStatus(SolicitudStatus solicitudStatus);

    Optional<SolicitudTatuador> findByUser_Id(Long userId);
}
