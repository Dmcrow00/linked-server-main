package com.linked.service;

import com.linked.dto.SweItemmenuDto;

import java.util.List;

/** Administración del menú de navegación del cliente web. */
public interface SweItemmenuService {

    /**
     * Ítems activos visibles para los roles indicados, en orden de visualización.
     * <p>
     * Devuelve la lista plana (padres e hijos): el cliente arma el árbol con
     * {@code idSupitemmenu}.
     */
    List<SweItemmenuDto.Response> findAllVisibles(List<Long> roleIds);

    SweItemmenuDto.Response findById(Long id);

    SweItemmenuDto.Response create(SweItemmenuDto.CreateRequest request);

    SweItemmenuDto.Response update(Long id, SweItemmenuDto.UpdateRequest request);

    /** Baja lógica. */
    void delete(Long id);
}
