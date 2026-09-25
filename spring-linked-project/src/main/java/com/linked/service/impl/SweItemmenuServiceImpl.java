package com.linked.service.impl;

import com.linked.dto.SweItemmenuDto;
import com.linked.exception.AppExceptions;
import com.linked.mapper.SweItemmenuMapper;
import com.linked.model.entity.SweItemmenu;
import com.linked.repository.SweItemmenuRepository;
import com.linked.service.SweItemmenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/** Administración del menú de navegación. La baja de un ítem es lógica. */
@Service
@RequiredArgsConstructor
public class SweItemmenuServiceImpl implements SweItemmenuService {

    private final SweItemmenuRepository itemmenuRepository;
    private final SweItemmenuMapper itemmenuMapper;

    @Override
    @Transactional(readOnly = true)
    public List<SweItemmenuDto.Response> findAllVisibles(List<Long> roleIds) {
        return itemmenuRepository.findAll().stream()
                .filter(SweItemmenu::isActive)
                .filter(item -> esVisiblePara(item, roleIds))
                .sorted(Comparator.comparing(SweItemmenu::getOrderNumber,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .map(itemmenuMapper::toResponse)
                .toList();
    }

    /**
     * Un ítem sin roles declarados es visible para cualquier usuario autenticado; si los declara,
     * alcanza con que el usuario tenga uno. Las entradas no numéricas se ignoran para que un dato
     * mal cargado esconda el ítem en lugar de romper el menú entero.
     */
    private boolean esVisiblePara(SweItemmenu item, List<Long> roleIds) {
        String permitidos = item.getRolesPermitidos();
        if (permitidos == null || permitidos.isBlank()) return true;
        return Arrays.stream(permitidos.split(","))
                .map(String::trim)
                .filter(s -> s.matches("\\d+"))
                .map(Long::valueOf)
                .anyMatch(roleIds::contains);
    }

    @Override
    @Transactional(readOnly = true)
    public SweItemmenuDto.Response findById(Long id) {
        return itemmenuMapper.toResponse(getActiveOrThrow(id));
    }

    @Override
    @Transactional
    public SweItemmenuDto.Response create(SweItemmenuDto.CreateRequest request) {
        SweItemmenu entity = itemmenuMapper.toEntity(request);
        if (request.idSupitemmenu() != null) {
            entity.setParentItem(itemmenuRepository.getReferenceById(request.idSupitemmenu()));
        }
        return itemmenuMapper.toResponse(itemmenuRepository.save(entity));
    }

    @Override
    @Transactional
    public SweItemmenuDto.Response update(Long id, SweItemmenuDto.UpdateRequest request) {
        SweItemmenu entity = getActiveOrThrow(id);
        itemmenuMapper.updateEntity(request, entity);
        if (request.idSupitemmenu() != null) {
            entity.setParentItem(itemmenuRepository.getReferenceById(request.idSupitemmenu()));
        }
        return itemmenuMapper.toResponse(itemmenuRepository.save(entity));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        SweItemmenu entity = getActiveOrThrow(id);
        entity.disable();
        itemmenuRepository.save(entity);
    }

    private SweItemmenu getActiveOrThrow(Long id) {
        SweItemmenu m = itemmenuRepository.findById(id)
                .orElseThrow(() -> new AppExceptions.ResourceNotFoundException("Item de menú no encontrado: " + id));
        if (!m.isActive()) {
            throw new AppExceptions.ResourceNotFoundException("Item de menú no encontrado: " + id);
        }
        return m;
    }
}
