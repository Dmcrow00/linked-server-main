package com.linked.repository;

import com.linked.model.entity.TrabajoFavorito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/** Acceso a los likes de trabajos ({@link TrabajoFavorito}). */
public interface TrabajoFavoritoRepository extends JpaRepository<TrabajoFavorito, Long> {

    boolean existsByCliente_IdAndTrabajo_Id(Long clienteId, Long trabajoId);

    Optional<TrabajoFavorito> findByCliente_IdAndTrabajo_Id(Long clienteId, Long trabajoId);

    /** IDs de los trabajos que le gustan al cliente. */
    @Query("SELECT tf.trabajo.id FROM TrabajoFavorito tf WHERE tf.cliente.id = :clienteId")
    List<Long> findTrabajoIdsByClienteId(@Param("clienteId") Long clienteId);

    int countByTrabajo_Id(Long trabajoId);
}
