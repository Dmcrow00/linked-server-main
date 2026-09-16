package com.linked.service.impl;

import com.linked.dto.EspecialidadDto;
import com.linked.exception.AppExceptions;
import com.linked.mapper.EspecialidadMapper;
import com.linked.model.entity.Especialidad;
import com.linked.repository.EspecialidadRepository;
import com.linked.service.EspecialidadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * ABM de especialidades. El nombre es único sin distinguir mayúsculas y la baja es lógica.
 */
@Service
@RequiredArgsConstructor
public class EspecialidadServiceImpl implements EspecialidadService {

    private final EspecialidadRepository especialidadRepository;
    private final EspecialidadMapper especialidadMapper;

    @Override
    @Transactional(readOnly = true)
    public List<EspecialidadDto.Response> findAll() {
        return especialidadMapper.toResponseList(
                especialidadRepository.findAllByStatusOrderByNombreAsc("true"));
    }

    @Override
    @Transactional(readOnly = true)
    public EspecialidadDto.Response findById(Long id) {
        return especialidadMapper.toResponse(getActiveOrThrow(id));
    }

    @Override
    @Transactional
    public EspecialidadDto.Response create(EspecialidadDto.CreateRequest request) {
        if (especialidadRepository.existsByNombreIgnoreCase(request.nombre())) {
            throw new AppExceptions.DuplicateResourceException("Ya existe una especialidad con ese nombre");
        }
        Especialidad entity = especialidadMapper.toEntity(request);
        return especialidadMapper.toResponse(especialidadRepository.save(entity));
    }

    @Override
    @Transactional
    public EspecialidadDto.Response update(Long id, EspecialidadDto.UpdateRequest request) {
        Especialidad entity = getActiveOrThrow(id);
        if (request.nombre() != null &&
            especialidadRepository.existsByNombreIgnoreCaseAndIdNot(request.nombre(), id)) {
            throw new AppExceptions.DuplicateResourceException("Ya existe una especialidad con ese nombre");
        }
        especialidadMapper.updateEntity(request, entity);
        return especialidadMapper.toResponse(especialidadRepository.save(entity));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Especialidad entity = getActiveOrThrow(id);
        entity.disable();
        especialidadRepository.save(entity);
    }

    /** Las especialidades dadas de baja se tratan como inexistentes. */
    private Especialidad getActiveOrThrow(Long id) {
        Especialidad e = especialidadRepository.findById(id)
                .orElseThrow(() -> new AppExceptions.ResourceNotFoundException("Especialidad no encontrada: " + id));
        if (!e.isActive()) {
            throw new AppExceptions.ResourceNotFoundException("Especialidad no encontrada: " + id);
        }
        return e;
    }
}
