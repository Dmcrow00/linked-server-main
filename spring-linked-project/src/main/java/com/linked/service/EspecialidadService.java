package com.linked.service;

import com.linked.dto.EspecialidadDto;

import java.util.List;

/** ABM de especialidades (estilos de tatuaje). */
public interface EspecialidadService {

    /** Especialidades activas ordenadas por nombre. */
    List<EspecialidadDto.Response> findAll();

    EspecialidadDto.Response findById(Long id);

    EspecialidadDto.Response create(EspecialidadDto.CreateRequest request);

    EspecialidadDto.Response update(Long id, EspecialidadDto.UpdateRequest request);

    /** Baja lógica. */
    void delete(Long id);
}
