package com.linked.service;

import com.linked.dto.TrabajoDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface TrabajosService {

    List<TrabajoDto.Response> getFeed(Long currentUserId);

    List<TrabajoDto.Response> getByTatuador(Long tatuadorId, Long currentUserId);

    TrabajoDto.Response getById(Long id, Long currentUserId);

    TrabajoDto.Response subirTrabajo(Long tatuadorId, TrabajoDto.CreateRequest request, MultipartFile[] fotos);

    void darLike(Long clienteId, Long trabajoId);

    void quitarLike(Long clienteId, Long trabajoId);

    List<Long> getMisLikesIds(Long clienteId);

    void delete(Long id);
}
