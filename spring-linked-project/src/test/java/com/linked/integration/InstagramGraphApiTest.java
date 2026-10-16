package com.linked.integration;

import com.dmc00.core.web.AppExceptions;
import com.linked.config.PropiedadesInstagram;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/** El adaptador de Instagram contra un servidor simulado: qué pide a Meta y cómo lee sus respuestas. */
class InstagramGraphApiTest {

    private MockRestServiceServer meta;
    private InstagramGraphApi api;

    @BeforeEach
    void setUp() {
        RestClient.Builder http = RestClient.builder();
        meta = MockRestServiceServer.bindTo(http).build();
        api = new InstagramGraphApi(http, new PropiedadesInstagram("app-1", "secreto-1",
                "https://api.linked.test/api/instagram/callback", "v21.0",
                new PropiedadesInstagram.Sincronizacion(null, null, null, true)));
    }

    @Test
    void laUrlDeAutorizacionPideElPermisoBasicoDeCuentasProfesionales() {
        String url = api.urlDeAutorizacion("estado.firmado");

        assertThat(url).startsWith("https://www.instagram.com/oauth/authorize?client_id=app-1")
                .contains("redirect_uri=https://api.linked.test/api/instagram/callback")
                .contains("scope=instagram_business_basic")
                .contains("state=estado.firmado");
    }

    @Test
    void elCodigoSeCanjeaPorUnTokenDeLargaDuracion() {
        meta.expect(requestTo("https://api.instagram.com/oauth/access_token"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().formDataContains(Map.of("code", "codigo-1", "client_secret", "secreto-1")))
                .andRespond(withSuccess("{\"access_token\": \"corto\", \"user_id\": 1789}", MediaType.APPLICATION_JSON));
        meta.expect(requestTo("https://graph.instagram.com/access_token?grant_type=ig_exchange_token&client_secret=secreto-1&access_token=corto"))
                .andRespond(withSuccess("{\"access_token\": \"largo\", \"expires_in\": 5184000}", MediaType.APPLICATION_JSON));

        InstagramApi.TokenDeAcceso token = api.canjearCodigo("codigo-1");

        assertThat(token.accessToken()).isEqualTo("largo");
        assertThat(token.segundosDeVida()).isEqualTo(5_184_000);
        assertThat(token.instagramUserId()).isEqualTo("1789");
        assertThat(token.toString()).doesNotContain("largo");
    }

    @Test
    void lasPublicacionesSeLeenDeTodasLasPaginasConLasImagenesDeCadaCarrusel() {
        meta.expect(requestTo("https://graph.instagram.com/v21.0/me/media?fields=id,caption,media_type,media_url,timestamp&limit=100"))
                .andExpect(header("Authorization", "Bearer tk"))
                .andRespond(withSuccess("""
                        {"data": [
                           {"id": "1", "caption": "Rosa", "media_type": "IMAGE", "media_url": "https://cdn.test/1.jpg",
                            "timestamp": "2026-10-01T15:00:00+0000"},
                           {"id": "2", "media_type": "CAROUSEL_ALBUM", "timestamp": "2026-10-02T15:00:00+0000"}],
                         "paging": {"next": "https://graph.instagram.com/v21.0/me/media?after=abc"}}
                        """, MediaType.APPLICATION_JSON));
        meta.expect(requestTo("https://graph.instagram.com/v21.0/2/children?fields=id,media_type,media_url"))
                .andRespond(withSuccess("""
                        {"data": [{"id": "2a", "media_type": "IMAGE", "media_url": "https://cdn.test/2a.jpg"},
                                  {"id": "2b", "media_type": "VIDEO", "media_url": "https://cdn.test/2b.mp4"}]}
                        """, MediaType.APPLICATION_JSON));
        meta.expect(requestTo("https://graph.instagram.com/v21.0/me/media?after=abc"))
                .andRespond(withSuccess("{\"data\": [{\"id\": \"3\", \"media_type\": \"VIDEO\"}]}", MediaType.APPLICATION_JSON));

        List<InstagramApi.Publicacion> publicaciones = api.publicaciones("tk");

        assertThat(publicaciones).extracting(InstagramApi.Publicacion::id).containsExactly("1", "2", "3");
        assertThat(publicaciones.get(0).imagenes()).containsExactly("https://cdn.test/1.jpg");
        assertThat(publicaciones.get(0).fecha()).isEqualTo(LocalDateTime.of(2026, 10, 1, 12, 0));
        assertThat(publicaciones.get(1).imagenes()).containsExactly("https://cdn.test/2a.jpg");
        assertThat(publicaciones.get(2).esVideo()).isTrue();
        meta.verify();
    }

    @Test
    void elError190DeMetaEsUnTokenVencidoYElRestoUnErrorDelServicioExterno() {
        meta.expect(requestTo("https://graph.instagram.com/refresh_access_token?grant_type=ig_refresh_token&access_token=viejo"))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON)
                        .body("{\"error\": {\"message\": \"Session has expired\", \"type\": \"OAuthException\", \"code\": 190}}"));
        meta.expect(requestTo("https://graph.instagram.com/refresh_access_token?grant_type=ig_refresh_token&access_token=otro"))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON)
                        .body("{\"error\": {\"message\": \"Invalid parameter\", \"code\": 100}}"));

        assertThatThrownBy(() -> api.renovar("viejo")).isInstanceOf(InstagramApi.TokenVencidoException.class);
        assertThatThrownBy(() -> api.renovar("otro")).isInstanceOf(AppExceptions.ServicioExternoException.class);
    }
}
