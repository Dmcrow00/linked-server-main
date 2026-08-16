package com.linked.service;

import com.linked.dto.EspecialidadDto;

import java.util.List;

public interface EspecialidadService {

    List<EspecialidadDto.Response> findAll();

    EspecialidadDto.Response findById(Long id);

    EspecialidadDto.Response create(EspecialidadDto.CreateRequest request);

    EspecialidadDto.Response update(Long id, EspecialidadDto.UpdateRequest request);

    void delete(Long id);
}
