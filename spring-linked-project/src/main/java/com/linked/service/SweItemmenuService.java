package com.linked.service;

import com.linked.dto.SweItemmenuDto;

import java.util.List;

public interface SweItemmenuService {

    List<SweItemmenuDto.Response> findAll();

    SweItemmenuDto.Response findById(Long id);

    SweItemmenuDto.Response create(SweItemmenuDto.CreateRequest request);

    SweItemmenuDto.Response update(Long id, SweItemmenuDto.UpdateRequest request);

    void delete(Long id);
}
