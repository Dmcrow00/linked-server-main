package com.linked.service.impl;

import com.linked.dto.TurnoMensajeDto;
import com.linked.dto.TurnoSesionDto;
import com.linked.exception.AppExceptions;
import com.linked.mapper.TurnoMensajeMapper;
import com.linked.mapper.TurnoSesionMapper;
import com.linked.model.entity.*;
import com.linked.model.enums.EstadoTurno;
import com.linked.repository.*;
import com.linked.service.TurnosService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

/**
 * Implementación de {@link TurnosService}.
 * <p>
 * Al crear entidades se usa siempre la instancia que devuelve {@code save()}: BaseEntity
 * inicializa {@code version} en 0, Spring Data las trata como existentes y hace {@code merge},
 * que devuelve una copia administrada. Si se agrega la instancia original a una colección con
 * cascade, Hibernate intenta persistirla otra vez al confirmar la transacción.
 */
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

        if (turnoTatuadorRepository.existsTurnoSolapado(tatuador.getId(), inicio, fin)) {
            throw new AppExceptions.DuplicateResourceException(
                    "El tatuador ya tiene un turno asignado que se solapa con ese horario.");
        }

        TurnoSesion turno = new TurnoSesion();
        turno.setCliente(cliente);
        turno.setFechaHoraInicio(inicio);
        turno.setFechaHoraFin(fin);
        turno.setDescripcionCliente(request.descripcionCliente());
        turno = turnoSesionRepository.save(turno);

        turno.getTurnoTatuadores().add(turnoTatuadorRepository.save(new TurnoTatuador(tatuador, turno)));

        if (imagenes != null) {
            for (MultipartFile img : imagenes) {
                if (img != null && !img.isEmpty()) {
                    ImagenRef ir = new ImagenRef();
                    ir.setImagePath(cloudinaryService.uploadImage(img, "referencias"));
                    ir.setTurnoSesion(turno);
                    turno.getImagenesRef().add(imagenRefRepository.save(ir));
                }
            }
        }

        return turnoSesionMapper.toResponse(turno);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TurnoSesionDto.Response> getTurnosByCliente(Long clienteId) {
        return turnoSesionRepository.findByClienteIdWithDetails(clienteId)
                .stream().map(turnoSesionMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TurnoSesionDto.Response> getTurnosByTatuador(Long tatuadorId) {
        return turnoSesionRepository.findByTatuadorIdWithDetails(tatuadorId)
                .stream().map(turnoSesionMapper::toResponse).toList();
    }

    @Override
    @Transactional
    public TurnoSesionDto.Response actualizarEstado(Long turnoId, TurnoSesionDto.GestionarRequest request,
                                                     Long tatuadorId) {
        validarEstado(request.estado());
        TurnoSesion turno = getTurnoOrThrow(turnoId);

        if (!turnoTatuadorRepository.existsByTatuador_IdAndTurnoSesion_Id(tatuadorId, turnoId)) {
            throw new AppExceptions.ForbiddenException("No tienes permiso para gestionar este turno");
        }

        turno.setEstado(request.estado());
        return turnoSesionMapper.toResponse(turnoSesionRepository.save(turno));
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
        if (request.estado() != null) {
            validarEstado(request.estado());
            turno.setEstado(request.estado());
        }
        return turnoSesionMapper.toResponse(turnoSesionRepository.save(turno));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TurnoMensajeDto.Response> getMensajes(Long turnoId, Long userId) {
        validarParticipante(getTurnoOrThrow(turnoId), userId);
        return turnoMensajeMapper.toResponseList(
                turnoMensajeRepository.findByTurnoSesion_IdOrderByCreationTimestampAsc(turnoId));
    }

    @Override
    @Transactional
    public TurnoMensajeDto.Response enviarMensaje(Long turnoId, Long userId,
                                                   TurnoMensajeDto.CreateRequest request) {
        TurnoSesion turno = getTurnoOrThrow(turnoId);
        validarParticipante(turno, userId);

        TurnoMensaje mensaje = new TurnoMensaje();
        mensaje.setTexto(request.texto());
        mensaje.setTurnoSesion(turno);
        mensaje.setUsuarioEnvia(sweUsrRepository.getReferenceById(userId));

        return turnoMensajeMapper.toResponse(turnoMensajeRepository.save(mensaje));
    }

    private TurnoSesion getTurnoOrThrow(Long id) {
        return turnoSesionRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new AppExceptions.ResourceNotFoundException("Turno no encontrado: " + id));
    }

    private void validarEstado(String estado) {
        boolean valido = Arrays.stream(EstadoTurno.values()).anyMatch(e -> e.name().equals(estado));
        if (!valido) {
            throw new AppExceptions.BadRequestException("Estado de turno inválido: " + estado);
        }
    }

    /** Solo el cliente que pidió el turno y los tatuadores asignados pueden usar su chat. */
    private void validarParticipante(TurnoSesion turno, Long userId) {
        boolean esCliente = turno.getCliente() != null && turno.getCliente().getId().equals(userId);
        boolean esTatuador = turno.getTurnoTatuadores().stream()
                .anyMatch(tt -> tt.getTatuador().getId().equals(userId));
        if (!esCliente && !esTatuador) {
            throw new AppExceptions.ForbiddenException("No tienes permiso para acceder al chat de este turno");
        }
    }
}
