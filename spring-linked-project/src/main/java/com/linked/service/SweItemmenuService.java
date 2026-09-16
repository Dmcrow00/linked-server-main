package com.linked.service;

import com.linked.dto.SweItemmenuDto;

import java.util.List;

/** Administración del menú de navegación del cliente web. */
public interface SweItemmenuService {

    /** Ítems activos de primer nivel, en orden de visualización. */
    List<SweItemmenuDto.Response> findAll();

    SweItemmenuDto.Response findById(Long id);

    SweItemmenuDto.Response create(SweItemmenuDto.CreateRequest request);

    SweItemmenuDto.Response update(Long id, SweItemmenuDto.UpdateRequest request);

    /** Baja lógica. */
    void delete(Long id);
}
