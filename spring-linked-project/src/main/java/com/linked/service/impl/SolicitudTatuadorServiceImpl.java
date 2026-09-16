package com.linked.service.impl;

import com.linked.dto.SolicitudTatuadorDto;
import com.linked.exception.AppExceptions;
import com.linked.mapper.SolicitudTatuadorMapper;
import com.linked.model.entity.*;
import com.linked.model.enums.SolicitudStatus;
import com.linked.repository.*;
import com.linked.service.SolicitudTatuadorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/** Revisión, por parte de un administrador, de las solicitudes para ser tatuador. */
@Slf4j
@Service
@RequiredArgsConstructor
public class SolicitudTatuadorServiceImpl implements SolicitudTatuadorService {

    private final SolicitudTatuadorRepository solicitudRepository;
    private final SweUsrRepository sweUsrRepository;
    private final SweRolRepository sweRolRepository;
    private final SweUsrRolRepository sweUsrRolRepository;
    private final EspecialidadRepository especialidadRepository;
    private final SolicitudTatuadorMapper solicitudMapper;

    @Override
    @Transactional(readOnly = true)
    public List<SolicitudTatuadorDto.Response> getAll() {
        return solicitudMapper.toResponseList(solicitudRepository.findAllByOrderByCreationTimestampDesc());
    }

    /**
     * Aprueba la solicitud: reemplaza el rol Cliente por Tatuador y asigna las especialidades
     * elegidas en el registro. Solo se pueden procesar solicitudes pendientes.
     */
    @Override
    @Transactional
    public void aprobar(Long id, String adminUsername) {
        SolicitudTatuador solicitud = getOrThrow(id);

        if (solicitud.getSolicitudStatus() != SolicitudStatus.pendiente) {
            throw new AppExceptions.BadRequestException("La solicitud ya fue procesada");
        }

        solicitud.setSolicitudStatus(SolicitudStatus.aprobada);
        solicitud.setAdminUser(adminUsername);
        solicitudRepository.save(solicitud);

        SweUsrapl user = solicitud.getUser();

        // Quitar rol Cliente
        sweUsrRolRepository.findByUsrapl_Id(user.getId()).stream()
                .filter(ur -> "Cliente".equals(ur.getRolapl().getDescription()))
                .forEach(ur -> sweUsrRolRepository.delete(ur));

        // Asignar rol Tatuador
        SweRolapl rolTatuador = sweRolRepository.findByDescription("Tatuador")
                .orElseThrow(() -> new AppExceptions.InternalException("Rol Tatuador no encontrado"));

        SweUsrrolapl nuevoRol = new SweUsrrolapl();
        nuevoRol.setUsrapl(user);
        nuevoRol.setRolapl(rolTatuador);
        sweUsrRolRepository.save(nuevoRol);

        // Asignar especialidades guardadas como arreglo JSON, ej. "[1, 2, 3]"
        if (solicitud.getEspecialidadesIds() != null && !solicitud.getEspecialidadesIds().isBlank()) {
            try {
                String raw = solicitud.getEspecialidadesIds().replaceAll("[\\[\\]\\s]", "");
                if (!raw.isEmpty()) {
                    List<Long> ids = new ArrayList<>();
                    for (String s : raw.split(",")) {
                        ids.add(Long.parseLong(s.trim()));
                    }
                    List<Especialidad> especialidades = especialidadRepository.findAllById(ids);
                    user.getEspecialidades().addAll(especialidades);
                    sweUsrRepository.save(user);
                }
            } catch (NumberFormatException e) {
                log.warn("Could not parse especialidades_ids for solicitud {}: {}", id, solicitud.getEspecialidadesIds());
            }
        }
    }

    /** Rechaza la solicitud dejando registro del administrador y del motivo. */
    @Override
    @Transactional
    public void rechazar(Long id, String adminUsername, String notas) {
        SolicitudTatuador solicitud = getOrThrow(id);

        if (solicitud.getSolicitudStatus() != SolicitudStatus.pendiente) {
            throw new AppExceptions.BadRequestException("La solicitud ya fue procesada");
        }

        solicitud.setSolicitudStatus(SolicitudStatus.rechazada);
        solicitud.setAdminUser(adminUsername);
        solicitud.setNotas(notas);
        solicitudRepository.save(solicitud);
    }

    private SolicitudTatuador getOrThrow(Long id) {
        return solicitudRepository.findById(id)
                .orElseThrow(() -> new AppExceptions.ResourceNotFoundException("Solicitud no encontrada: " + id));
    }
}
