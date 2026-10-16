package com.linked.service.impl;

import com.dmc00.core.data.BaseEntity;
import com.dmc00.core.swe.model.entity.SweRolapl;
import com.dmc00.core.swe.model.entity.SweUsrapl;
import com.dmc00.core.swe.repository.SweRolRepository;
import com.dmc00.core.swe.service.SweUsrService;
import com.dmc00.core.web.AppExceptions;
import com.linked.dto.SolicitudTatuadorDto;
import com.linked.mapper.SolicitudTatuadorMapper;
import com.linked.model.entity.PerfilUsuario;
import com.linked.model.entity.SolicitudTatuador;
import com.linked.model.enums.SolicitudStatus;
import com.linked.repository.EspecialidadRepository;
import com.linked.repository.PerfilUsuarioRepository;
import com.linked.repository.SolicitudTatuadorRepository;
import com.linked.service.SolicitudTatuadorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/** Revisión, por parte de un administrador, de las solicitudes para ser tatuador. */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SolicitudTatuadorServiceImpl implements SolicitudTatuadorService {

    private final SolicitudTatuadorRepository solicitudRepository;
    private final SweRolRepository roles;
    private final SweUsrService sweUsrService;
    private final PerfilUsuarioRepository perfiles;
    private final EspecialidadRepository especialidadRepository;
    private final SolicitudTatuadorMapper solicitudMapper;

    @Override
    public List<SolicitudTatuadorDto.Response> getAll() {
        List<SolicitudTatuador> solicitudes = solicitudRepository.findAllByOrderByCreationTimestampDesc();
        return solicitudMapper.toResponseList(solicitudes, perfiles.porUsuario(
                solicitudes.stream().map(solicitud -> solicitud.getUser().getId()).distinct().toList()));
    }

    /**
     * Aprueba la solicitud: reemplaza el rol Cliente por Tatuador y asigna las especialidades
     * elegidas en el registro. Solo se pueden procesar solicitudes pendientes.
     */
    @Override
    @Transactional
    public void aprobar(Long id, String adminUsername) {
        SolicitudTatuador solicitud = pendienteOError(id);
        solicitud.setSolicitudStatus(SolicitudStatus.aprobada);
        solicitud.setAdminUser(adminUsername);

        SweUsrapl usuario = solicitud.getUser();
        Long rolTatuador = roles.findByDescription(UserServiceImpl.TATUADOR)
                .orElseThrow(() -> new AppExceptions.InternalException("No existe el rol Tatuador"))
                .getId();
        List<Long> rolesNuevos = Stream.concat(
                        usuario.getRolesVigentes().stream()
                                .filter(rol -> !UserServiceImpl.CLIENTE.equals(rol.getDescription()))
                                .map(SweRolapl::getId),
                        Stream.of(rolTatuador))
                .distinct()
                .toList();
        sweUsrService.updateRoles(usuario.getId(), rolesNuevos);

        List<Long> especialidades = idsDeEspecialidades(solicitud);
        if (!especialidades.isEmpty()) {
            PerfilUsuario perfil = perfiles.findById(usuario.getId()).orElseGet(() -> new PerfilUsuario(usuario));
            especialidadRepository.findAllById(especialidades).stream()
                    .filter(BaseEntity::isActive)
                    .filter(especialidad -> !perfil.getEspecialidades().contains(especialidad))
                    .forEach(perfil.getEspecialidades()::add);
            perfiles.save(perfil);
        }
    }

    /** Rechaza la solicitud dejando registro del administrador y del motivo. */
    @Override
    @Transactional
    public void rechazar(Long id, String adminUsername, String notas) {
        SolicitudTatuador solicitud = pendienteOError(id);
        solicitud.setSolicitudStatus(SolicitudStatus.rechazada);
        solicitud.setAdminUser(adminUsername);
        solicitud.setNotas(notas);
    }

    private SolicitudTatuador pendienteOError(Long id) {
        SolicitudTatuador solicitud = solicitudRepository.findById(id)
                .orElseThrow(() -> new AppExceptions.ResourceNotFoundException("Solicitud", "id", id));
        if (solicitud.getSolicitudStatus() != SolicitudStatus.pendiente) {
            throw new AppExceptions.BadRequestException("La solicitud ya fue procesada");
        }
        return solicitud;
    }

    /** Las especialidades elegidas se guardaron como arreglo JSON, p. ej. {@code "[1, 2, 3]"}. */
    private List<Long> idsDeEspecialidades(SolicitudTatuador solicitud) {
        List<Long> ids = new ArrayList<>();
        String guardadas = solicitud.getEspecialidadesIds();
        if (guardadas == null) {
            return ids;
        }
        for (String id : guardadas.replaceAll("[\\[\\]\\s]", "").split(",")) {
            if (id.isEmpty()) {
                continue;
            }
            try {
                ids.add(Long.parseLong(id));
            } catch (NumberFormatException ex) {
                log.warn("Especialidad inválida en la solicitud {}: '{}'", solicitud.getId(), id);
            }
        }
        return ids;
    }
}
