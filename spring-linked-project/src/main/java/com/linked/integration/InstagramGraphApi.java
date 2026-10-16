package com.linked.integration;

import com.dmc00.core.web.AppExceptions;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.linked.config.PropiedadesInstagram;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.regex.Pattern;

/**
 * {@link InstagramApi} sobre la Graph API de Meta (Instagram API con inicio de sesión de Instagram).
 * <p>
 * El token viaja en el header {@code Authorization} salvo en los dos endpoints de canje y
 * renovación, que solo lo aceptan como parámetro.
 */
@Component
public class InstagramGraphApi implements InstagramApi {

    private static final String AUTORIZAR = "https://www.instagram.com/oauth/authorize";
    private static final String CANJE_DE_CODIGO = "https://api.instagram.com/oauth/access_token";
    private static final String GRAPH = "https://graph.instagram.com";
    private static final String PERMISOS = "instagram_business_basic";

    /** Vigencia estándar de un token de larga duración (60 días), si Instagram no la informa. */
    static final long VIDA_POR_DEFECTO_SEG = 5_183_944L;

    /** Tope de páginas de publicaciones (100 por página): corta un {@code next} que no termina. */
    static final int MAXIMO_DE_PAGINAS = 50;

    /** Error OAuth 190 de Meta: token vencido, revocado o inválido. */
    private static final Pattern TOKEN_VENCIDO = Pattern.compile("\"code\"\\s*:\\s*190\\b");

    private static final DateTimeFormatter FECHA_DE_META = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ");
    private static final ZoneId ZONA_DE_LA_APP = ZoneId.of("America/Argentina/Buenos_Aires");

    private final RestClient http;
    private final PropiedadesInstagram propiedades;

    public InstagramGraphApi(RestClient.Builder http, PropiedadesInstagram propiedades) {
        this.http = http.build();
        this.propiedades = propiedades;
    }

    @Override
    public String urlDeAutorizacion(String state) {
        return UriComponentsBuilder.fromUriString(AUTORIZAR)
                .queryParam("client_id", propiedades.appId())
                .queryParam("redirect_uri", propiedades.redirectUri())
                .queryParam("scope", PERMISOS)
                .queryParam("response_type", "code")
                .queryParam("state", state)
                .encode()
                .toUriString();
    }

    @Override
    public TokenDeAcceso canjearCodigo(String code) {
        MultiValueMap<String, String> formulario = new LinkedMultiValueMap<>();
        formulario.add("client_id", propiedades.appId());
        formulario.add("client_secret", propiedades.appSecret());
        formulario.add("grant_type", "authorization_code");
        formulario.add("redirect_uri", propiedades.redirectUri());
        formulario.add("code", code);
        RespuestaDeToken corto = llamar(() -> http.post()
                .uri(CANJE_DE_CODIGO)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(formulario)
                .retrieve()
                .body(RespuestaDeToken.class));

        // El token del canje dura una hora: se cambia por uno de larga duración.
        RespuestaDeToken largo = llamar(() -> http.get()
                .uri(GRAPH + "/access_token?grant_type=ig_exchange_token&client_secret={secreto}&access_token={token}",
                        propiedades.appSecret(), tokenDe(corto))
                .retrieve()
                .body(RespuestaDeToken.class));
        return new TokenDeAcceso(tokenDe(largo), vidaDe(largo),
                corto.userId() != null ? String.valueOf(corto.userId()) : null);
    }

    @Override
    public TokenDeAcceso renovar(String accessToken) {
        RespuestaDeToken renovado = llamar(() -> http.get()
                .uri(GRAPH + "/refresh_access_token?grant_type=ig_refresh_token&access_token={token}", accessToken)
                .retrieve()
                .body(RespuestaDeToken.class));
        return new TokenDeAcceso(tokenDe(renovado), vidaDe(renovado), null);
    }

    @Override
    public Cuenta cuenta(String accessToken) {
        RespuestaDeCuenta cuenta = llamar(() -> http.get()
                .uri(GRAPH + "/{version}/me?fields=user_id,username,profile_picture_url", propiedades.graphApiVersion())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .body(RespuestaDeCuenta.class));
        if (cuenta == null) {
            throw new AppExceptions.ServicioExternoException("Instagram no devolvió los datos de la cuenta", null);
        }
        String id = cuenta.userId() != null ? String.valueOf(cuenta.userId()) : cuenta.id();
        return new Cuenta(id, cuenta.username(), cuenta.profilePictureUrl());
    }

    @Override
    public List<Publicacion> publicaciones(String accessToken) {
        List<Publicacion> todas = new ArrayList<>();
        URI pagina = UriComponentsBuilder.fromUriString(GRAPH + "/{version}/me/media")
                .queryParam("fields", "id,caption,media_type,media_url,timestamp")
                .queryParam("limit", 100)
                .buildAndExpand(propiedades.graphApiVersion())
                .encode()
                .toUri();
        for (int leidas = 0; pagina != null && leidas < MAXIMO_DE_PAGINAS; leidas++) {
            URI actual = pagina;
            PaginaDeMedios respuesta = llamar(() -> http.get()
                    .uri(actual)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .body(PaginaDeMedios.class));
            if (respuesta == null || respuesta.data() == null) {
                break;
            }
            for (Medio medio : respuesta.data()) {
                todas.add(new Publicacion(medio.id(), medio.mediaType(), medio.caption(),
                        imagenesDe(medio, accessToken), fechaDe(medio.timestamp())));
            }
            pagina = respuesta.paging() != null && respuesta.paging().next() != null
                    ? URI.create(respuesta.paging().next())
                    : null;
        }
        return todas;
    }

    @Override
    public byte[] descargar(String url) {
        return llamar(() -> http.get().uri(URI.create(url)).retrieve().body(byte[].class));
    }

    private List<String> imagenesDe(Medio medio, String accessToken) {
        return switch (medio.mediaType() == null ? "" : medio.mediaType()) {
            case "IMAGE" -> medio.mediaUrl() != null ? List.of(medio.mediaUrl()) : List.of();
            case "CAROUSEL_ALBUM" -> {
                PaginaDeMedios hijos = llamar(() -> http.get()
                        .uri(GRAPH + "/{version}/{id}/children?fields=id,media_type,media_url",
                                propiedades.graphApiVersion(), medio.id())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .retrieve()
                        .body(PaginaDeMedios.class));
                yield hijos == null || hijos.data() == null ? List.of() : hijos.data().stream()
                        .filter(hijo -> "IMAGE".equals(hijo.mediaType()) && hijo.mediaUrl() != null)
                        .map(Medio::mediaUrl)
                        .toList();
            }
            default -> List.of();
        };
    }

    /** Traduce los errores de Meta: token vencido aparte (hay que desvincular), el resto como 502. */
    private static <T> T llamar(Supplier<T> pedido) {
        try {
            return pedido.get();
        } catch (HttpClientErrorException ex) {
            if (TOKEN_VENCIDO.matcher(ex.getResponseBodyAsString()).find()) {
                throw new TokenVencidoException("El token de Instagram venció o fue revocado");
            }
            throw new AppExceptions.ServicioExternoException("Instagram rechazó la solicitud: " + ex.getStatusCode(), ex);
        } catch (RestClientException ex) {
            throw new AppExceptions.ServicioExternoException("No se pudo comunicar con Instagram", ex);
        }
    }

    private static String tokenDe(RespuestaDeToken respuesta) {
        if (respuesta == null || respuesta.accessToken() == null) {
            throw new AppExceptions.ServicioExternoException("Instagram no devolvió un token de acceso", null);
        }
        return respuesta.accessToken();
    }

    private static long vidaDe(RespuestaDeToken respuesta) {
        return respuesta.expiresIn() != null ? respuesta.expiresIn() : VIDA_POR_DEFECTO_SEG;
    }

    /** Meta informa {@code 2024-05-01T13:45:00+0000}; se guarda en la hora local de la app. */
    static LocalDateTime fechaDe(String timestamp) {
        if (timestamp == null) {
            return null;
        }
        try {
            return OffsetDateTime.parse(timestamp, FECHA_DE_META).atZoneSameInstant(ZONA_DE_LA_APP).toLocalDateTime();
        } catch (DateTimeParseException ex) {
            return null;
        }
    }

    // ─── JSON de Meta ─────────────────────────────────────────────────────────

    record RespuestaDeToken(
            @JsonProperty("access_token") String accessToken,
            @JsonProperty("expires_in") Long expiresIn,
            @JsonProperty("user_id") Object userId) {
    }

    record RespuestaDeCuenta(
            String id,
            @JsonProperty("user_id") Object userId,
            String username,
            @JsonProperty("profile_picture_url") String profilePictureUrl) {
    }

    record PaginaDeMedios(List<Medio> data, Paginado paging) {
    }

    record Medio(
            String id,
            String caption,
            @JsonProperty("media_type") String mediaType,
            @JsonProperty("media_url") String mediaUrl,
            String timestamp) {
    }

    record Paginado(String next) {
    }
}
