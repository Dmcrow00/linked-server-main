package com.linked.service.impl;

import com.dmc00.core.data.BaseEntity;
import com.dmc00.core.swe.repository.SweUsrRepository;
import com.dmc00.core.web.AppExceptions;
import com.linked.dto.TrabajoDto;
import com.linked.integration.AlmacenDeImagenes;
import com.linked.mapper.TrabajoMapper;
import com.linked.model.entity.PerfilUsuario;
import com.linked.model.entity.Trabajo;
import com.linked.model.entity.TrabajoFavorito;
import com.linked.model.entity.TrabajoFoto;
import com.linked.repository.PerfilUsuarioRepository;
import com.linked.repository.TrabajoFavoritoRepository;
import com.linked.repository.TrabajoRepository;
import com.linked.service.TrabajosService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Set;

/** Implementación de {@link TrabajosService}. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TrabajosServiceImpl implements TrabajosService {

    static final int MAXIMO_DE_FOTOS = 5;
    static final int TAMANIO_DEL_FEED = 50;

    private final TrabajoRepository trabajoRepository;
    private final TrabajoFavoritoRepository favoritoRepository;
    private final SweUsrRepository usuarios;
    private final PerfilUsuarioRepository perfiles;
    private final AlmacenDeImagenes imagenes;
    private final TrabajoMapper trabajoMapper;

    @Override
    public List<TrabajoDto.Response> getFeed(Long currentUserId) {
        return aDto(trabajoRepository.findAllActive(PageRequest.of(0, TAMANIO_DEL_FEED)).getContent(), currentUserId);
    }

    @Override
    public List<TrabajoDto.Response> getByTatuador(Long tatuadorId, Long currentUserId) {
        return aDto(trabajoRepository.findByTatuador_IdAndStatusOrderByCreationTimestampDesc(tatuadorId, BaseEntity.ACTIVO),
                currentUserId);
    }

    @Override
    public TrabajoDto.Response getById(Long id, Long currentUserId) {
        return aDto(List.of(activoOError(id)), currentUserId).getFirst();
    }

    @Override
    @Transactional
    public TrabajoDto.Response subirTrabajo(Long tatuadorId, TrabajoDto.CreateRequest request, MultipartFile[] fotos) {
        if (fotos == null || fotos.length == 0) {
            throw new AppExceptions.BadRequestException("Se requiere al menos una foto");
        }
        if (fotos.length > MAXIMO_DE_FOTOS) {
            throw new AppExceptions.BadRequestException("Máximo " + MAXIMO_DE_FOTOS + " fotos por trabajo");
        }
        Trabajo trabajo = new Trabajo();
        trabajo.setDescripcion(request.descripcion());
        trabajo.setTatuador(usuarios.getReferenceById(tatuadorId));
        for (MultipartFile foto : fotos) {
            TrabajoFoto trabajoFoto = new TrabajoFoto();
            trabajoFoto.setImagePath(imagenes.subir(foto, "trabajos"));
            trabajoFoto.setTrabajo(trabajo);
            trabajo.getFotos().add(trabajoFoto);
        }
        return aDto(List.of(trabajoRepository.save(trabajo)), tatuadorId).getFirst();
    }

    @Override
    @Transactional
    public void darLike(Long clienteId, Long trabajoId) {
        if (favoritoRepository.existsByCliente_IdAndTrabajo_Id(clienteId, trabajoId)) {
            throw new AppExceptions.DuplicateResourceException("Ya marcaste este trabajo como favorito");
        }
        TrabajoFavorito favorito = new TrabajoFavorito();
        favorito.setCliente(usuarios.getReferenceById(clienteId));
        favorito.setTrabajo(activoOError(trabajoId));
        favoritoRepository.save(favorito);
    }

    @Override
    @Transactional
    public void quitarLike(Long clienteId, Long trabajoId) {
        TrabajoFavorito favorito = favoritoRepository.findByCliente_IdAndTrabajo_Id(clienteId, trabajoId)
                .orElseThrow(() -> new AppExceptions.ResourceNotFoundException("Favorito", "trabajo", trabajoId));
        favoritoRepository.delete(favorito);
    }

    @Override
    public List<Long> getMisLikesIds(Long clienteId) {
        return favoritoRepository.findTrabajoIdsByClienteId(clienteId);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        activoOError(id).disable();
    }

    private Trabajo activoOError(Long id) {
        return trabajoRepository.findById(id)
                .filter(BaseEntity::isActive)
                .orElseThrow(() -> new AppExceptions.ResourceNotFoundException("Trabajo", "id", id));
    }

    /** Busca una sola vez los perfiles de los tatuadores y los likes de quien consulta. */
    private List<TrabajoDto.Response> aDto(List<Trabajo> trabajos, Long currentUserId) {
        Map<Long, PerfilUsuario> perfilesDeTatuadores = perfiles.porUsuario(
                trabajos.stream().map(trabajo -> trabajo.getTatuador().getId()).distinct().toList());
        Set<Long> meGustan = currentUserId != null
                ? Set.copyOf(favoritoRepository.findTrabajoIdsByClienteId(currentUserId))
                : Set.of();
        return trabajos.stream()
                .map(trabajo -> trabajoMapper.toResponse(trabajo, perfilesDeTatuadores, meGustan))
                .toList();
    }
}
