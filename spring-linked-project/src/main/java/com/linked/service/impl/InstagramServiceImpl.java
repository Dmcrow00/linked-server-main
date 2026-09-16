package com.linked.service.impl;

import com.linked.dto.InstagramStatusDto;
import com.linked.exception.AppExceptions;
import com.linked.model.entity.InstagramToken;
import com.linked.model.entity.Trabajo;
import com.linked.model.entity.TrabajoFoto;
import com.linked.repository.InstagramTokenRepository;
import com.linked.repository.SweUsrRepository;
import com.linked.repository.TrabajoRepository;
import com.linked.service.InstagramService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Implementación de {@link InstagramService} sobre la API Graph de Instagram. */
@Slf4j
@Service
@RequiredArgsConstructor
public class InstagramServiceImpl implements InstagramService {

    private static final String GRAPH_BASE = "https://graph.instagram.com";
    private static final String OAUTH_BASE  = "https://api.instagram.com/oauth";

    /** Vigencia estándar de un token de larga duración (60 días), si la API no la informa. */
    private static final long EXPIRACION_POR_DEFECTO_SEG = 5183944L;

    private final InstagramTokenRepository tokenRepository;
    private final SweUsrRepository sweUsrRepository;
    private final TrabajoRepository trabajoRepository;
    private final CloudinaryServiceImpl cloudinaryService;
    private final WebClient.Builder webClientBuilder;

    @Value("${app.instagram.app-id:}")
    private String appId;

    @Value("${app.instagram.app-secret:}")
    private String appSecret;

    @Value("${app.instagram.redirect-uri:http://localhost:8080/api/instagram/callback}")
    private String redirectUri;

    @Value("${app.instagram.graph-api-version:v21.0}")
    private String graphApiVersion;

    @Value("${app.frontend.base-url:http://localhost:4200}")
    private String frontendBaseUrl;

    @Override
    public String initiateOAuth(Long tatuadorId) {
        return UriComponentsBuilder.fromUriString("https://www.instagram.com/oauth/authorize")
                .queryParam("client_id", appId)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("scope", "instagram_basic,instagram_content_publish")
                .queryParam("response_type", "code")
                .queryParam("state", tatuadorId.toString())
                .toUriString();
    }

    @Override
    @Transactional
    @SuppressWarnings("unchecked")
    public void handleCallback(String code, String state) {
        Long tatuadorId = Long.parseLong(state);

        // 1. Código de autorización → token de corta duración
        Map<?, ?> shortToken = webClientBuilder.build()
                .post()
                .uri(OAUTH_BASE + "/access_token")
                .bodyValue("client_id=" + appId +
                           "&client_secret=" + appSecret +
                           "&grant_type=authorization_code" +
                           "&redirect_uri=" + redirectUri +
                           "&code=" + code)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        if (shortToken == null || !shortToken.containsKey("access_token")) {
            throw new AppExceptions.InternalException("No se pudo obtener el token de Instagram");
        }

        String shortAccessToken = (String) shortToken.get("access_token");

        // 2. Token de corta duración → token de larga duración
        Map<?, ?> longToken = webClientBuilder.build()
                .get()
                .uri(GRAPH_BASE + "/access_token" +
                     "?grant_type=ig_exchange_token" +
                     "&client_secret=" + appSecret +
                     "&access_token=" + shortAccessToken)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        if (longToken == null || !longToken.containsKey("access_token")) {
            throw new AppExceptions.InternalException("No se pudo obtener el token de larga duración");
        }

        String accessToken = (String) longToken.get("access_token");
        Object expiresInRaw = longToken.get("expires_in");
        long expiresIn = expiresInRaw instanceof Number n ? n.longValue() : EXPIRACION_POR_DEFECTO_SEG;

        // 3. ID de la cuenta de Instagram
        Map<?, ?> userInfo = webClientBuilder.build()
                .get()
                .uri(GRAPH_BASE + "/me?fields=id&access_token=" + accessToken)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        String instagramUserId = userInfo != null ? (String) userInfo.get("id") : "";

        InstagramToken token = tokenRepository.findByTatuador_Id(tatuadorId)
                .orElse(new InstagramToken());
        token.setTatuador(sweUsrRepository.getReferenceById(tatuadorId));
        token.setAccessToken(accessToken);
        token.setInstagramUserId(instagramUserId);
        token.setTokenExpiresAt(LocalDateTime.now().plusSeconds(expiresIn));
        tokenRepository.save(token);
    }

    @Override
    @Transactional
    @SuppressWarnings("unchecked")
    public void syncPosts(Long tatuadorId) {
        InstagramToken token = tokenRepository.findByTatuador_Id(tatuadorId).orElse(null);
        if (token == null) return;

        try {
            Map<?, ?> response = webClientBuilder.build()
                    .get()
                    .uri(GRAPH_BASE + "/" + graphApiVersion + "/me/media" +
                         "?fields=id,caption,media_type,media_url,timestamp" +
                         "&access_token=" + token.getAccessToken())
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response == null) return;

            List<Map<?, ?>> posts = (List<Map<?, ?>>) response.get("data");
            if (posts == null) return;

            for (Map<?, ?> post : posts) {
                String mediaId = (String) post.get("id");
                String mediaType = (String) post.get("media_type");

                if (!"IMAGE".equals(mediaType) && !"CAROUSEL_ALBUM".equals(mediaType)) continue;
                if (trabajoRepository.existsByInstagramMediaId(mediaId)) continue;

                String mediaUrl = (String) post.get("media_url");
                String caption  = (String) post.get("caption");

                Trabajo trabajo = new Trabajo();
                trabajo.setTatuador(sweUsrRepository.getReferenceById(tatuadorId));
                trabajo.setInstagramMediaId(mediaId);
                trabajo.setDescripcion(caption);

                // La imagen se copia a Cloudinary porque las URLs de Instagram vencen.
                if (mediaUrl != null) {
                    try {
                        byte[] imageBytes = webClientBuilder.build()
                                .get().uri(mediaUrl)
                                .retrieve().bodyToMono(byte[].class).block();

                        if (imageBytes != null) {
                            String cloudUrl = cloudinaryService.uploadBytes(imageBytes, "instagram");
                            TrabajoFoto foto = new TrabajoFoto();
                            foto.setImagePath(cloudUrl);
                            foto.setTrabajo(trabajo);
                            trabajo.getFotos().add(foto);
                        }
                    } catch (Exception e) {
                        log.warn("Could not download Instagram image {}: {}", mediaId, e.getMessage());
                    }
                }

                trabajoRepository.save(trabajo);
            }
        } catch (Exception e) {
            log.error("Error syncing Instagram posts for tatuador {}: {}", tatuadorId, e.getMessage());
        }
    }

    @Override
    @Async
    public void syncAsync(Long tatuadorId) {
        syncPosts(tatuadorId);
    }

    @Override
    @Transactional(readOnly = true)
    public InstagramStatusDto getStatus(Long tatuadorId) {
        return tokenRepository.findByTatuador_Id(tatuadorId)
                .map(t -> new InstagramStatusDto(
                        true,
                        t.getInstagramUserId(),
                        t.getTokenExpiresAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
                .orElse(new InstagramStatusDto(false, null, null));
    }

    @Override
    @Transactional
    public void disconnect(Long tatuadorId) {
        tokenRepository.deleteByTatuador_Id(tatuadorId);
    }

    /** Todos los días a las 3:00 renueva los tokens que vencen en menos de 7 días. */
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    @SuppressWarnings("unchecked")
    public void refreshExpiringTokens() {
        LocalDateTime threshold = LocalDateTime.now().plusDays(7);
        List<InstagramToken> expiring = tokenRepository.findExpiringBefore(threshold);

        for (InstagramToken token : expiring) {
            try {
                Map<?, ?> refreshed = webClientBuilder.build()
                        .get()
                        .uri(GRAPH_BASE + "/refresh_access_token" +
                             "?grant_type=ig_refresh_token" +
                             "&access_token=" + token.getAccessToken())
                        .retrieve()
                        .bodyToMono(Map.class)
                        .block();

                if (refreshed != null && refreshed.containsKey("access_token")) {
                    Object expiresInRaw = refreshed.get("expires_in");
                    long expiresIn = expiresInRaw instanceof Number n ? n.longValue() : EXPIRACION_POR_DEFECTO_SEG;
                    token.setAccessToken((String) refreshed.get("access_token"));
                    token.setTokenExpiresAt(LocalDateTime.now().plusSeconds(expiresIn));
                    tokenRepository.save(token);
                    log.info("Refreshed Instagram token for tatuador {}", token.getTatuador().getId());
                }
            } catch (Exception e) {
                log.error("Failed to refresh Instagram token for tatuador {}: {}",
                        token.getTatuador().getId(), e.getMessage());
            }
        }
    }
}
