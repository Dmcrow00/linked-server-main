package com.linked.service.impl;

import com.linked.dto.TrabajoDto;
import com.linked.exception.AppExceptions;
import com.linked.mapper.TrabajoMapper;
import com.linked.model.entity.Trabajo;
import com.linked.model.entity.TrabajoFavorito;
import com.linked.model.entity.TrabajoFoto;
import com.linked.repository.SweUsrRepository;
import com.linked.repository.TrabajoFavoritoRepository;
import com.linked.repository.TrabajoRepository;
import com.linked.service.TrabajosService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TrabajosServiceImpl implements TrabajosService {

    private static final int MAX_FOTOS = 5;

    private final TrabajoRepository trabajoRepository;
    private final TrabajoFavoritoRepository favoritoRepository;
    private final SweUsrRepository sweUsrRepository;
    private final CloudinaryServiceImpl cloudinaryService;
    private final TrabajoMapper trabajoMapper;

    @Override
    @Transactional(readOnly = true)
    public List<TrabajoDto.Response> getFeed(Long currentUserId) {
        Pageable pageable = PageRequest.of(0, 50);
        List<Trabajo> trabajos = trabajoRepository.findAllActive(pageable).getContent();
        return enrichWithLikes(trabajos, currentUserId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrabajoDto.Response> getByTatuador(Long tatuadorId, Long currentUserId) {
        List<Trabajo> trabajos = trabajoRepository
                .findByTatuador_IdAndStatusOrderByCreationTimestampDesc(tatuadorId, "true");
        return enrichWithLikes(trabajos, currentUserId);
    }

    @Override
    @Transactional(readOnly = true)
    public TrabajoDto.Response getById(Long id, Long currentUserId) {
        Trabajo trabajo = getActiveOrThrow(id);
        TrabajoDto.Response response = trabajoMapper.toResponse(trabajo);
        if (currentUserId != null) {
            boolean liked = favoritoRepository.existsByCliente_IdAndTrabajo_Id(currentUserId, id);
            return new TrabajoDto.Response(
                    response.id(), response.descripcion(), response.instagramMediaId(),
                    response.fotos(), response.tatuador(), response.likesCount(), liked,
                    response.creationTimestamp(), response.status());
        }
        return response;
    }

    @Override
    @Transactional
    public TrabajoDto.Response subirTrabajo(Long tatuadorId, TrabajoDto.CreateRequest request, MultipartFile[] fotos) {
        if (fotos == null || fotos.length == 0) {
            throw new AppExceptions.BadRequestException("Se requiere al menos una foto");
        }
        if (fotos.length > MAX_FOTOS) {
            throw new AppExceptions.BadRequestException("Máximo " + MAX_FOTOS + " fotos por trabajo");
        }

        Trabajo trabajo = new Trabajo();
        trabajo.setDescripcion(request.descripcion());
        trabajo.setTatuador(sweUsrRepository.getReferenceById(tatuadorId));

        List<TrabajoFoto> fotoEntities = new ArrayList<>();
        for (MultipartFile foto : fotos) {
            String url = cloudinaryService.uploadImage(foto, "trabajos");
            TrabajoFoto tf = new TrabajoFoto();
            tf.setImagePath(url);
            tf.setTrabajo(trabajo);
            fotoEntities.add(tf);
        }
        trabajo.setFotos(fotoEntities);

        return trabajoMapper.toResponse(trabajoRepository.save(trabajo));
    }

    @Override
    @Transactional
    public void darLike(Long clienteId, Long trabajoId) {
        if (favoritoRepository.existsByCliente_IdAndTrabajo_Id(clienteId, trabajoId)) {
            throw new AppExceptions.DuplicateResourceException("Ya marcaste este trabajo como favorito");
        }
        TrabajoFavorito fav = new TrabajoFavorito();
        fav.setCliente(sweUsrRepository.getReferenceById(clienteId));
        fav.setTrabajo(trabajoRepository.getReferenceById(trabajoId));
        favoritoRepository.save(fav);
    }

    @Override
    @Transactional
    public void quitarLike(Long clienteId, Long trabajoId) {
        TrabajoFavorito fav = favoritoRepository.findByCliente_IdAndTrabajo_Id(clienteId, trabajoId)
                .orElseThrow(() -> new AppExceptions.ResourceNotFoundException("Favorito no encontrado"));
        favoritoRepository.delete(fav);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> getMisLikesIds(Long clienteId) {
        return favoritoRepository.findTrabajoIdsByClienteId(clienteId);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Trabajo trabajo = getActiveOrThrow(id);
        trabajo.disable();
        trabajoRepository.save(trabajo);
    }

    private Trabajo getActiveOrThrow(Long id) {
        Trabajo t = trabajoRepository.findById(id)
                .orElseThrow(() -> new AppExceptions.ResourceNotFoundException("Trabajo no encontrado: " + id));
        if (!t.isActive()) {
            throw new AppExceptions.ResourceNotFoundException("Trabajo no encontrado: " + id);
        }
        return t;
    }

    private List<TrabajoDto.Response> enrichWithLikes(List<Trabajo> trabajos, Long currentUserId) {
        Set<Long> likedIds = currentUserId != null
                ? Set.copyOf(favoritoRepository.findTrabajoIdsByClienteId(currentUserId))
                : Set.of();

        return trabajos.stream().map(t -> {
            TrabajoDto.Response r = trabajoMapper.toResponse(t);
            boolean liked = likedIds.contains(t.getId());
            return new TrabajoDto.Response(
                    r.id(), r.descripcion(), r.instagramMediaId(),
                    r.fotos(), r.tatuador(), r.likesCount(), liked,
                    r.creationTimestamp(), r.status());
        }).toList();
    }
}
