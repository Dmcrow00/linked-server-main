package com.linked.service;

import com.linked.dto.SolicitudTatuadorDto;

import java.util.List;

public interface SolicitudTatuadorService {

    List<SolicitudTatuadorDto.Response> getAll();

    void aprobar(Long id, String adminUsername);

    void rechazar(Long id, String adminUsername, String notas);
}
