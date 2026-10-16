package com.linked.service.impl;

import com.dmc00.core.swe.model.entity.SweUsrapl;
import com.dmc00.core.swe.repository.SweUsrRepository;
import com.dmc00.core.web.AppExceptions;
import com.linked.dto.TurnoMensajeDto;
import com.linked.dto.TurnoSesionDto;
import com.linked.integration.AlmacenDeImagenes;
import com.linked.mapper.TurnoMensajeMapper;
import com.linked.mapper.TurnoSesionMapper;
import com.linked.model.entity.ImagenRef;
import com.linked.model.entity.PerfilUsuario;
import com.linked.model.entity.TurnoMensaje;
import com.linked.model.entity.TurnoSesion;
import com.linked.model.entity.TurnoTatuador;
import com.linked.model.enums.EstadoTurno;
import com.linked.repository.ImagenRefRepository;
import com.linked.repository.PerfilUsuarioRepository;
import com.linked.repository.TurnoMensajeRepository;
import com.linked.repository.TurnoSesionRepository;
import com.linked.repository.TurnoTatuadorRepository;
import com.linked.service.TurnosService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

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
@Transactional(readOnly = true)
public class TurnosServiceImpl implements TurnosService {

    /** Imágenes de referencia por pedido, como en Node. */
    static final int MAXIMO_DE_IMAGENES = 3;

    private final TurnoSesionRepository turnoSesionRepository;
    private final TurnoTatuadorRepository turnoTatuadorRepository;
    private final TurnoMensajeRepository turnoMensajeRepository;
    private final ImagenRefRepository imagenRefRepository;
    private final SweUsrRepository usuarios;
    private final PerfilUsuarioRepository perfiles;
    private final AlmacenDeImagenes imagenes;
    private final TurnoSesionMapper turnoSesionMapper;
    private final TurnoMensajeMapper turnoMensajeMapper;

    @Override
    @Transactional
    public TurnoSesionDto.Response solicitarTurno(Long clienteId, TurnoSesionDto.CreateRequest request,
                                                  MultipartFile[] imagenesDeReferencia) {
        SweUsrapl tatuador = usuarios.findById(request.tatuadorId())
                .filter(usuario -> usuario.isEnabled() && usuario.hasRole("Tatuador"))
                .orElseThrow(() -> new AppExceptions.ResourceNotFoundException("Tatuador", "id", request.tatuadorId()));
        LocalDateTime inicio = fechaHora(request.fechaHoraInicio(), "fechaHoraInicio");
        LocalDateTime fin = fechaHora(request.fechaHoraFin(), "fechaHoraFin");
        if (!fin.isAfter(inicio)) {
            throw new AppExceptions.BadRequestException("El turno tiene que terminar después de empezar");
        }
        if (imagenesDeReferencia != null && imagenesDeReferencia.length > MAXIMO_DE_IMAGENES) {
            throw new AppExceptions.BadRequestException("Máximo " + MAXIMO_DE_IMAGENES + " imágenes de referencia por turno");
        }
        if (turnoTatuadorRepository.existsTurnoSolapado(tatuador.getId(), inicio, fin)) {
            throw new AppExceptions.ConflictException(
                    "El tatuador ya tiene un turno asignado que se solapa con ese horario.", "TURNO_SOLAPADO");
        }

        TurnoSesion turno = new TurnoSesion();
        turno.setCliente(usuarios.getReferenceById(clienteId));
        turno.setFechaHoraInicio(inicio);
        turno.setFechaHoraFin(fin);
        turno.setDescripcionCliente(request.descripcionCliente());
        turno = turnoSesionRepository.save(turno);

        turno.getTurnoTatuadores().add(turnoTatuadorRepository.save(new TurnoTatuador(tatuador, turno)));

        if (imagenesDeReferencia != null) {
            for (MultipartFile imagen : imagenesDeReferencia) {
                if (imagen != null && !imagen.isEmpty()) {
                    ImagenRef referencia = new ImagenRef();
                    referencia.setImagePath(imagenes.subir(imagen, "referencias"));
                    referencia.setTurnoSesion(turno);
                    turno.getImagenesRef().add(imagenRefRepository.save(referencia));
                }
            }
        }
        return aDto(turno);
    }

    @Override
    public List<TurnoSesionDto.Response> getTurnosByCliente(Long clienteId) {
        return aDto(turnoSesionRepository.findByClienteIdWithDetails(clienteId));
    }

    @Override
    public List<TurnoSesionDto.Response> getTurnosByTatuador(Long tatuadorId) {
        return aDto(turnoSesionRepository.findByTatuadorIdWithDetails(tatuadorId));
    }

    @Override
    @Transactional
    public TurnoSesionDto.Response actualizarEstado(Long turnoId, TurnoSesionDto.GestionarRequest request,
                                                    Long tatuadorId) {
        TurnoSesion turno = turnoOError(turnoId);
        if (!turnoTatuadorRepository.existsByTatuador_IdAndTurnoSesion_Id(tatuadorId, turnoId)) {
            throw new AppExceptions.ForbiddenException("No tienes permiso para gestionar este turno");
        }
        cambiarEstado(turno, request.estado());
        return aDto(turno);
    }

    @Override
    @Transactional
    public TurnoSesionDto.Response update(Long turnoId, TurnoSesionDto.UpdateRequest request, Long userId) {
        TurnoSesion turno = turnoOError(turnoId);
        validarParticipante(turno, userId);
        if (request.fechaHoraInicio() != null) {
            turno.setFechaHoraInicio(fechaHora(request.fechaHoraInicio(), "fechaHoraInicio"));
        }
        if (request.fechaHoraFin() != null) {
            turno.setFechaHoraFin(fechaHora(request.fechaHoraFin(), "fechaHoraFin"));
        }
        if (request.descripcionCliente() != null) turno.setDescripcionCliente(request.descripcionCliente());
        if (request.estado() != null) {
            // Aceptar o rechazar el turno es potestad del tatuador; el cliente solo edita su pedido.
            if (!turnoTatuadorRepository.existsByTatuador_IdAndTurnoSesion_Id(userId, turnoId)) {
                throw new AppExceptions.ForbiddenException("Solo el tatuador asignado puede cambiar el estado del turno");
            }
            cambiarEstado(turno, request.estado());
        }
        return aDto(turno);
    }

    @Override
    public List<TurnoMensajeDto.Response> getMensajes(Long turnoId, Long userId) {
        validarParticipante(turnoOError(turnoId), userId);
        return turnoMensajeMapper.toResponseList(
                turnoMensajeRepository.findByTurnoSesion_IdOrderByCreationTimestampAsc(turnoId));
    }

    @Override
    @Transactional
    public TurnoMensajeDto.Response enviarMensaje(Long turnoId, Long userId, TurnoMensajeDto.CreateRequest request) {
        TurnoSesion turno = turnoOError(turnoId);
        validarParticipante(turno, userId);
        TurnoMensaje mensaje = new TurnoMensaje();
        mensaje.setTexto(request.texto());
        mensaje.setTurnoSesion(turno);
        mensaje.setUsuarioEnvia(usuarios.getReferenceById(userId));
        return turnoMensajeMapper.toResponse(turnoMensajeRepository.save(mensaje));
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private TurnoSesion turnoOError(Long id) {
        return turnoSesionRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new AppExceptions.ResourceNotFoundException("Turno", "id", id));
    }

    /** Como en Node: un turno completado o cancelado ya no cambia de estado (409). */
    private static void cambiarEstado(TurnoSesion turno, String nombreDelEstado) {
        EstadoTurno nuevo = EstadoTurno.desde(nombreDelEstado)
                .orElseThrow(() -> new AppExceptions.BadRequestException("Estado de turno inválido: " + nombreDelEstado));
        EstadoTurno actual = EstadoTurno.desde(turno.getEstado()).orElse(EstadoTurno.Pendiente);
        if (!actual.puedePasarA(nuevo)) {
            throw new AppExceptions.ConflictException(
                    "El turno ya está " + actual + " y no se puede modificar.", "TURNO_CERRADO");
        }
        turno.setEstado(nuevo.name());
    }

    /** Solo el cliente que pidió el turno y los tatuadores asignados pueden verlo o modificarlo. */
    private static void validarParticipante(TurnoSesion turno, Long userId) {
        boolean esCliente = turno.getCliente() != null && turno.getCliente().getId().equals(userId);
        boolean esTatuador = turno.getTurnoTatuadores().stream()
                .anyMatch(asignacion -> asignacion.getTatuador().getId().equals(userId));
        if (!esCliente && !esTatuador) {
            throw new AppExceptions.ForbiddenException("No tienes permiso para acceder a este turno");
        }
    }

    /** Fechas en ISO-8601 local ({@code 2026-10-15T18:30:00}), como las manda el cliente. */
    private static LocalDateTime fechaHora(String valor, String campo) {
        try {
            return LocalDateTime.parse(valor, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (DateTimeParseException ex) {
            throw new AppExceptions.BadRequestException(
                    "El campo " + campo + " tiene que ser una fecha y hora ISO-8601 (2026-10-15T18:30:00)");
        }
    }

    private TurnoSesionDto.Response aDto(TurnoSesion turno) {
        return aDto(List.of(turno)).getFirst();
    }

    /** Busca una sola vez los perfiles de clientes y tatuadores de la lista. */
    private List<TurnoSesionDto.Response> aDto(List<TurnoSesion> turnos) {
        List<Long> participantes = new ArrayList<>();
        for (TurnoSesion turno : turnos) {
            Stream.concat(
                            Stream.ofNullable(turno.getCliente()),
                            turno.getTurnoTatuadores().stream().map(TurnoTatuador::getTatuador))
                    .map(SweUsrapl::getId)
                    .forEach(participantes::add);
        }
        Map<Long, PerfilUsuario> perfilesDeParticipantes = perfiles.porUsuario(participantes.stream().distinct().toList());
        return turnos.stream().map(turno -> turnoSesionMapper.toResponse(turno, perfilesDeParticipantes)).toList();
    }
}
