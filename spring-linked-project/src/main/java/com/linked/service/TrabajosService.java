package com.linked.service;

import com.linked.dto.TrabajoDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Portfolio de trabajos de los tatuadores: publicación, feed y likes.
 * En las consultas, {@code currentUserId} es {@code null} cuando no hay sesión y se usa
 * para indicar en cada trabajo si el usuario le dio like.
 */
public interface TrabajosService {

    /** Últimos 50 trabajos activos de todos los tatuadores. */
    List<TrabajoDto.Response> getFeed(Long currentUserId);

    List<TrabajoDto.Response> getByTatuador(Long tatuadorId, Long currentUserId);

    TrabajoDto.Response getById(Long id, Long currentUserId);

    /** Publica un trabajo con entre 1 y 5 fotos, que se suben a Cloudinary. */
    TrabajoDto.Response subirTrabajo(Long tatuadorId, TrabajoDto.CreateRequest request, MultipartFile[] fotos);

    void darLike(Long clienteId, Long trabajoId);

    void quitarLike(Long clienteId, Long trabajoId);

    /** IDs de los trabajos a los que el cliente les dio like. */
    List<Long> getMisLikesIds(Long clienteId);

    /** Baja lógica del trabajo. */
    void delete(Long id);
}
