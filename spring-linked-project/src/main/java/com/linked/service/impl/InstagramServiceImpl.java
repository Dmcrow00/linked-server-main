package com.linked.service.impl;

import com.dmc00.core.data.BaseEntity;
import com.dmc00.core.security.JwtService;
import com.dmc00.core.swe.model.entity.SweUsrapl;
import com.dmc00.core.swe.repository.SweUsrRepository;
import com.dmc00.core.web.AppExceptions;
import com.linked.config.PropiedadesInstagram;
import com.linked.dto.InstagramStatusDto;
import com.linked.dto.SincronizacionInstagramDto;
import com.linked.integration.AlmacenDeImagenes;
import com.linked.integration.InstagramApi.Publicacion;
import com.linked.integration.InstagramApi;
import com.linked.model.entity.InstagramToken;
import com.linked.model.entity.PerfilUsuario;
import com.linked.model.entity.Trabajo;
import com.linked.model.entity.TrabajoFoto;
import com.linked.repository.InstagramTokenRepository;
import com.linked.repository.PerfilUsuarioRepository;
import com.linked.repository.TrabajoRepository;
import com.linked.service.InstagramService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementación de {@link InstagramService}.
 * <p>
 * Las llamadas a Instagram y a Cloudinary van fuera de las transacciones (pueden tardar
 * segundos): cada escritura en la base corre en su propia transacción corta con
 * {@link TransactionTemplate}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InstagramServiceImpl implements InstagramService {

    /**
     * Prefijo del subject del {@code state} de OAuth. Tiene más de 25 caracteres, el largo máximo
     * de un username: así un {@code state} nunca sirve como token de sesión de alguien.
     */
    static final String PREFIJO_DEL_STATE = "estado-oauth-de-instagram:";

    /** El {@code state} solo tiene que sobrevivir al ida y vuelta con Instagram. */
    static final Duration VIGENCIA_DEL_STATE = Duration.ofMinutes(10);

    private final InstagramApi instagram;
    private final AlmacenDeImagenes imagenes;
    private final InstagramTokenRepository tokens;
    private final SweUsrRepository usuarios;
    private final PerfilUsuarioRepository perfiles;
    private final TrabajoRepository trabajos;
    private final JwtService jwt;
    private final TransactionTemplate transacciones;
    private final ApplicationEventPublisher eventos;
    private final PropiedadesInstagram propiedades;
    private final Clock reloj;

    @Override
    public String initiateOAuth(Long tatuadorId) {
        String state = jwt.generateToken(PREFIJO_DEL_STATE + tatuadorId, Map.of(), VIGENCIA_DEL_STATE);
        return instagram.urlDeAutorizacion(state);
    }

    @Override
    public void handleCallback(String code, String state) {
        Long tatuadorId = tatuadorDelState(state);
        if (tatuadorId == null) {
            throw new AppExceptions.BadRequestException(
                    "El enlace de autorización venció o no es válido. Volvé a intentar la vinculación.");
        }
        InstagramApi.TokenDeAcceso acceso = instagram.canjearCodigo(code);
        InstagramApi.Cuenta cuenta = instagram.cuenta(acceso.accessToken());
        String foto = copiarFotoDePerfil(cuenta, tatuadorId);

        transacciones.executeWithoutResult(estado -> {
            SweUsrapl tatuador = usuarios.findById(tatuadorId)
                    .filter(SweUsrapl::isEnabled)
                    .orElseThrow(() -> new AppExceptions.ResourceNotFoundException("Tatuador", "id", tatuadorId));
            InstagramToken token = tokens.findByTatuador_Id(tatuadorId).orElseGet(InstagramToken::new);
            token.setTatuador(tatuador);
            token.setAccessToken(acceso.accessToken());
            token.setInstagramUserId(acceso.instagramUserId() != null ? acceso.instagramUserId() : cuenta.id());
            token.setTokenExpiresAt(LocalDateTime.now(reloj).plusSeconds(acceso.segundosDeVida()));
            tokens.save(token);

            // Mientras la cuenta esté vinculada, Instagram es la identidad visible del tatuador.
            PerfilUsuario perfil = perfiles.findById(tatuadorId).orElseGet(() -> new PerfilUsuario(tatuador));
            if (cuenta.username() != null && !cuenta.username().isBlank()) {
                perfil.setInstagramHandle(cuenta.username());
            }
            perfiles.save(perfil);
            if (foto != null) {
                tatuador.setProfilePhoto(foto);
            }
        });
        eventos.publishEvent(new CuentaVinculada(tatuadorId));
    }

    @Override
    public SincronizacionInstagramDto syncPosts(Long tatuadorId) {
        InstagramToken token = tokens.findByTatuador_Id(tatuadorId)
                .orElseThrow(() -> new AppExceptions.ResourceNotFoundException("Cuenta de Instagram", "tatuador", tatuadorId));
        List<Publicacion> publicaciones;
        try {
            publicaciones = instagram.publicaciones(token.getAccessToken());
        } catch (InstagramApi.TokenVencidoException ex) {
            disconnect(tatuadorId);
            log.warn("Token de Instagram vencido del tatuador {}: se desvinculó la cuenta", tatuadorId);
            throw new AppExceptions.ConflictException(
                    "El token de Instagram venció. Vinculá tu cuenta nuevamente desde Mi Perfil.", "INSTAGRAM_TOKEN_VENCIDO");
        }

        Set<String> siguenEnInstagram = publicaciones.stream().map(Publicacion::id).collect(Collectors.toSet());
        int eliminados = darDeBajaLosBorrados(tatuadorId, siguenEnInstagram);

        int nuevos = 0;
        int omitidos = 0;
        for (Publicacion publicacion : publicaciones) {
            if (publicacion.esVideo() || trabajos.existsByInstagramMediaId(publicacion.id())) {
                omitidos++;
                continue;
            }
            List<String> copias = copiarImagenes(publicacion);
            if (copias.isEmpty()) {
                omitidos++;
                continue;
            }
            transacciones.executeWithoutResult(estado -> importar(tatuadorId, publicacion, copias));
            nuevos++;
        }
        if (nuevos > 0 || eliminados > 0) {
            log.info("Instagram del tatuador {}: {} nuevos, {} dados de baja, {} omitidos",
                    tatuadorId, nuevos, eliminados, omitidos);
        }
        return new SincronizacionInstagramDto(nuevos, eliminados, omitidos);
    }

    @Override
    public InstagramStatusDto getStatus(Long tatuadorId) {
        return tokens.findByTatuador_Id(tatuadorId)
                .map(token -> new InstagramStatusDto(true, token.getInstagramUserId(),
                        token.getTokenExpiresAt() != null
                                ? token.getTokenExpiresAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                                : null))
                .orElse(new InstagramStatusDto(false, null, null));
    }

    @Override
    public void disconnect(Long tatuadorId) {
        transacciones.executeWithoutResult(estado -> {
            tokens.deleteByTatuador_Id(tatuadorId);
            perfiles.findById(tatuadorId).ifPresent(perfil -> perfil.setInstagramHandle(null));
            // La foto era la de Instagram (mientras está vinculada no se puede cambiar a mano).
            usuarios.findById(tatuadorId).ifPresent(usuario -> usuario.setProfilePhoto(null));
        });
    }

    @Override
    public List<Long> tatuadoresVinculados() {
        return tokens.findIdsDeTatuadores();
    }

    @Override
    public void mantener(Long tatuadorId) {
        InstagramToken token = tokens.findByTatuador_Id(tatuadorId).orElse(null);
        if (token == null) {
            return;
        }
        LocalDateTime ahora = LocalDateTime.now(reloj);
        if (!token.getTokenExpiresAt().isAfter(ahora)) {
            if (propiedades.sincronizacion().desvincularVencidos()) {
                disconnect(tatuadorId);
                log.warn("Token de Instagram vencido del tatuador {}: se desvinculó la cuenta", tatuadorId);
            } else {
                log.warn("Token de Instagram vencido del tatuador {}: no se desvincula (desvincular-vencidos=false)",
                        tatuadorId);
            }
            return;
        }
        if (token.getTokenExpiresAt().isBefore(ahora.plus(propiedades.sincronizacion().renovarAntesDe()))) {
            try {
                InstagramApi.TokenDeAcceso renovado = instagram.renovar(token.getAccessToken());
                transacciones.executeWithoutResult(estado -> tokens.findByTatuador_Id(tatuadorId).ifPresent(vigente -> {
                    vigente.setAccessToken(renovado.accessToken());
                    vigente.setTokenExpiresAt(LocalDateTime.now(reloj).plusSeconds(renovado.segundosDeVida()));
                }));
            } catch (InstagramApi.TokenVencidoException ex) {
                disconnect(tatuadorId);
                log.warn("Instagram rechazó renovar el token del tatuador {}: se desvinculó la cuenta", tatuadorId);
                return;
            }
        }
        syncPosts(tatuadorId);
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    /** El id del tatuador que inició el flujo, o {@code null} si el {@code state} no es válido o venció. */
    private Long tatuadorDelState(String state) {
        try {
            String subject = jwt.extractClaim(state, Claims::getSubject);
            if (subject == null || !subject.startsWith(PREFIJO_DEL_STATE)) {
                return null;
            }
            return Long.valueOf(subject.substring(PREFIJO_DEL_STATE.length()));
        } catch (JwtException | IllegalArgumentException ex) {
            log.warn("State de OAuth de Instagram inválido: {}", ex.getMessage());
            return null;
        }
    }

    /** Si la descarga falla se conserva la foto anterior: no vale la pena cortar la vinculación por la imagen. */
    private String copiarFotoDePerfil(InstagramApi.Cuenta cuenta, Long tatuadorId) {
        if (cuenta.fotoDePerfil() == null || cuenta.fotoDePerfil().isBlank()) {
            return null;
        }
        try {
            return imagenes.subir(instagram.descargar(cuenta.fotoDePerfil()), "profile-photos");
        } catch (RuntimeException ex) {
            log.warn("No se pudo copiar la foto de perfil de Instagram del tatuador {}: {}", tatuadorId, ex.getMessage());
            return null;
        }
    }

    /** Las URLs de Instagram vencen: cada imagen se copia a Cloudinary. Las que fallan se saltean. */
    private List<String> copiarImagenes(Publicacion publicacion) {
        List<String> copias = new ArrayList<>();
        for (String url : publicacion.imagenes()) {
            try {
                copias.add(imagenes.subir(instagram.descargar(url), "instagram"));
            } catch (RuntimeException ex) {
                log.warn("No se pudo copiar una imagen de la publicación {} de Instagram: {}", publicacion.id(), ex.getMessage());
            }
        }
        return copias;
    }

    private void importar(Long tatuadorId, Publicacion publicacion, List<String> copias) {
        Trabajo trabajo = new Trabajo();
        trabajo.setTatuador(usuarios.getReferenceById(tatuadorId));
        trabajo.setInstagramMediaId(publicacion.id());
        trabajo.setDescripcion(publicacion.descripcion());
        for (String url : copias) {
            TrabajoFoto foto = new TrabajoFoto();
            foto.setImagePath(url);
            foto.setTrabajo(trabajo);
            trabajo.getFotos().add(foto);
        }
        trabajos.save(trabajo);
    }

    /**
     * Da de baja los trabajos importados cuya publicación ya no está en Instagram. Node los borraba;
     * acá es baja lógica: dejan de verse pero se conservan los likes.
     */
    private int darDeBajaLosBorrados(Long tatuadorId, Set<String> siguenEnInstagram) {
        Integer dadosDeBaja = transacciones.execute(estado -> {
            int cantidad = 0;
            for (Trabajo trabajo : trabajos.findByTatuador_IdAndInstagramMediaIdIsNotNullAndStatus(tatuadorId, BaseEntity.ACTIVO)) {
                if (!siguenEnInstagram.contains(trabajo.getInstagramMediaId())) {
                    trabajo.disable();
                    cantidad++;
                }
            }
            return cantidad;
        });
        return dadosDeBaja != null ? dadosDeBaja : 0;
    }
}
