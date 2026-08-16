package com.linked.service;

import com.linked.dto.TurnoMensajeDto;
import com.linked.dto.TurnoSesionDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface TurnosService {

    TurnoSesionDto.Response solicitarTurno(Long clienteId, TurnoSesionDto.CreateRequest request, MultipartFile[] imagenes);

    List<TurnoSesionDto.Response> getTurnosByCliente(Long clienteId);

    List<TurnoSesionDto.Response> getTurnosByTatuador(Long tatuadorId);

    TurnoSesionDto.Response actualizarEstado(Long turnoId, TurnoSesionDto.GestionarRequest request, Long tatuadorId);

    TurnoSesionDto.Response update(Long turnoId, TurnoSesionDto.UpdateRequest request);

    List<TurnoMensajeDto.Response> getMensajes(Long turnoId);

    TurnoMensajeDto.Response enviarMensaje(Long turnoId, Long userId, TurnoMensajeDto.CreateRequest request);
}
