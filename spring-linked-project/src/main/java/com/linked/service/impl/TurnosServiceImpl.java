package com.linked.service.impl;

import com.linked.dto.TurnoMensajeDto;
import com.linked.dto.TurnoSesionDto;
import com.linked.exception.AppExceptions;
import com.linked.mapper.TurnoMensajeMapper;
import com.linked.mapper.TurnoSesionMapper;
import com.linked.model.entity.*;
import com.linked.repository.*;
import com.linked.service.TurnosService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TurnosServiceImpl implements TurnosService {

    private final TurnoSesionRepository turnoSesionRepository;
    private final TurnoTatuadorRepository turnoTatuadorRepository;
    private final TurnoMensajeRepository turnoMensajeRepository;
    private final ImagenRefRepository imagenRefRepository;
    private final SweUsrRepository sweUsrRepository;
    private final CloudinaryServiceImpl cloudinaryService;
    private final TurnoSesionMapper turnoSesionMapper;
    private final TurnoMensajeMapper turnoMensajeMapper;

    @Override
    @Transactional
    public TurnoSesionDto.Response solicitarTurno(Long clienteId, TurnoSesionDto.CreateRequest request,
                                                   MultipartFile[] imagenes) {
        SweUsrapl cliente = sweUsrRepository.getReferenceById(clienteId);
        SweUsrapl tatuador = sweUsrRepository.findById(request.tatuadorId())
                .orElseThrow(() -> new AppExceptions.ResourceNotFoundException("Tatuador no encontrado"));

        LocalDateTime inicio = LocalDateTime.parse(request.fechaHoraInicio(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        LocalDateTime fin = LocalDateTime.parse(request.fechaHoraFin(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        TurnoSesion turno = new TurnoSesion();
        turno.setCliente(cliente);
        turno.setFechaHoraInicio(inicio);
        turno.setFechaHoraFin(fin);
        turno.setDescripcionCliente(request.descripcionCliente());
        turno = turnoSesionRepository.save(turno);

        // Asignar tatuador
        TurnoTatuador tt = new TurnoTatuador(tatuador, turno);
        turnoTatuadorRepository.save(tt);
        turno.getTurnoTatuadores().add(tt);

        // Subir imágenes de referencia
        if (imagenes != null) {
            for (MultipartFile img : imagenes) {
                if (img != null && !img.isEmpty()) {
                    String url = cloudinaryService.uploadImage(img, "referencias");
                    ImagenRef ir = new ImagenRef();
                    ir.setImagePath(url);
                    ir.setTurnoSesion(turno);
                    imagenRefRepository.save(ir);
                    turno.getImagenesRef().add(ir);
                }
            }
        }

        return turnoSesionMapper.toResponse(turno, null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TurnoSesionDto.Response> getTurnosByCliente(Long clienteId) {
        return turnoSesionRepository.findByClienteIdWithDetails(clienteId)
                .stream().map(ts -> turnoSesionMapper.toResponse(ts, null)).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TurnoSesionDto.Response> getTurnosByTatuador(Long tatuadorId) {
        return turnoSesionRepository.findByTatuadorIdWithDetails(tatuadorId)
                .stream().map(ts -> turnoSesionMapper.toResponse(ts, null)).toList();
    }

    @Override
    @Transactional
    public TurnoSesionDto.Response actualizarEstado(Long turnoId, TurnoSesionDto.GestionarRequest request,
                                                     Long tatuadorId) {
        TurnoSesion turno = getTurnoOrThrow(turnoId);

        if (!turnoTatuadorRepository.existsByTatuador_IdAndTurnoSesion_Id(tatuadorId, turnoId)) {
            throw new AppExceptions.ForbiddenException("No tienes permiso para gestionar este turno");
        }

        turno.setEstado(request.estado());
        return turnoSesionMapper.toResponse(turnoSesionRepository.save(turno), null);
    }

    @Override
    @Transactional
    public TurnoSesionDto.Response update(Long turnoId, TurnoSesionDto.UpdateRequest request) {
        TurnoSesion turno = getTurnoOrThrow(turnoId);
        if (request.fechaHoraInicio() != null) {
            turno.setFechaHoraInicio(LocalDateTime.parse(request.fechaHoraInicio(),
                    DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
        if (request.fechaHoraFin() != null) {
            turno.setFechaHoraFin(LocalDateTime.parse(request.fechaHoraFin(),
                    DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
        if (request.descripcionCliente() != null) turno.setDescripcionCliente(request.descripcionCliente());
        if (request.estado() != null) turno.setEstado(request.estado());
        return turnoSesionMapper.toResponse(turnoSesionRepository.save(turno), null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TurnoMensajeDto.Response> getMensajes(Long turnoId) {
        return turnoMensajeMapper.toResponseList(
                turnoMensajeRepository.findByTurnoSesion_IdOrderByCreationTimestampAsc(turnoId));
    }

    @Override
    @Transactional
    public TurnoMensajeDto.Response enviarMensaje(Long turnoId, Long userId,
                                                   TurnoMensajeDto.CreateRequest request) {
        TurnoSesion turno = getTurnoOrThrow(turnoId);
        SweUsrapl usuario = sweUsrRepository.getReferenceById(userId);

        TurnoMensaje mensaje = new TurnoMensaje();
        mensaje.setTexto(request.texto());
        mensaje.setTurnoSesion(turno);
        mensaje.setUsuarioEnvia(usuario);

        return turnoMensajeMapper.toResponse(turnoMensajeRepository.save(mensaje));
    }

    private TurnoSesion getTurnoOrThrow(Long id) {
        return turnoSesionRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new AppExceptions.ResourceNotFoundException("Turno no encontrado: " + id));
    }
}
