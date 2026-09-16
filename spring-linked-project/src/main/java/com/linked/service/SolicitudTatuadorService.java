package com.linked.service;

import com.linked.dto.SolicitudTatuadorDto;

import java.util.List;

/** Revisión de solicitudes para ser tatuador. */
public interface SolicitudTatuadorService {

    /** Todas las solicitudes, de la más reciente a la más antigua. */
    List<SolicitudTatuadorDto.Response> getAll();

    /** Aprueba una solicitud pendiente y convierte al usuario en Tatuador. */
    void aprobar(Long id, String adminUsername);

    /** Rechaza una solicitud pendiente, con notas opcionales para el usuario. */
    void rechazar(Long id, String adminUsername, String notas);
}
