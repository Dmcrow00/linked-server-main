package com.linked.service;

import com.linked.dto.TurnoMensajeDto;
import com.linked.dto.TurnoSesionDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/** Turnos: solicitud por parte del cliente, gestión por el tatuador y chat entre ambos. */
public interface TurnosService {

    /**
     * Crea un turno Pendiente asignado al tatuador y sube las imágenes de referencia.
     * Lo rechaza si el tatuador ya tiene un turno activo que se superpone con ese horario.
     */
    TurnoSesionDto.Response solicitarTurno(Long clienteId, TurnoSesionDto.CreateRequest request, MultipartFile[] imagenes);

    List<TurnoSesionDto.Response> getTurnosByCliente(Long clienteId);

    List<TurnoSesionDto.Response> getTurnosByTatuador(Long tatuadorId);

    /** Cambia el estado del turno; solo puede hacerlo un tatuador asignado. */
    TurnoSesionDto.Response actualizarEstado(Long turnoId, TurnoSesionDto.GestionarRequest request, Long tatuadorId);

    /** Actualización parcial de fechas, descripción o estado. */
    TurnoSesionDto.Response update(Long turnoId, TurnoSesionDto.UpdateRequest request);

    /** Mensajes del chat en orden cronológico; solo para el cliente o un tatuador del turno. */
    List<TurnoMensajeDto.Response> getMensajes(Long turnoId, Long userId);

    /** Envía un mensaje al chat; solo para el cliente o un tatuador del turno. */
    TurnoMensajeDto.Response enviarMensaje(Long turnoId, Long userId, TurnoMensajeDto.CreateRequest request);
}
