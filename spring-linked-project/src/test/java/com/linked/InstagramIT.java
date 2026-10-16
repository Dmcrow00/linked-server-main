package com.linked;

import com.linked.integration.InstagramApi.Publicacion;
import com.linked.job.SincronizacionDeInstagram;
import com.linked.repository.InstagramTokenRepository;
import com.linked.soporte.DoblesDePrueba;
import com.linked.soporte.InstagramApiFalsa;
import com.linked.soporte.PruebaDeIntegracion;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Vinculación con Instagram, importación de publicaciones y sincronización programada. */
class InstagramIT extends PruebaDeIntegracion {

    /** La hora del reloj fijo de los tests: los vencimientos se comparan contra ella. */
    private static final LocalDateTime HOY = DoblesDePrueba.AHORA;

    @Autowired
    InstagramApiFalsa instagram;

    @Autowired
    InstagramTokenRepository tokens;

    @Autowired
    SincronizacionDeInstagram sincronizacion;

    @Autowired
    TransactionTemplate transacciones;

    @Test
    void alVincularSeGuardaElTokenYElPerfilTomaElUsuarioYLaFotoDeInstagram() throws Exception {
        Long tatuador = crearUsuario("Tatuador");

        String token = vincularInstagram(tatuador);

        assertThat(tokens.findByTatuador_Id(tatuador)).hasValueSatisfying(guardado -> {
            assertThat(guardado.getAccessToken()).isEqualTo(token);
            assertThat(guardado.getTokenExpiresAt()).isEqualTo(HOY.plusSeconds(InstagramApiFalsa.SESENTA_DIAS));
        });
        mvc.perform(get("/api/users/" + tatuador).header(HttpHeaders.AUTHORIZATION, sesionDe(tatuador)))
                .andExpect(jsonPath("$.data.instagramHandle").value("tinta.fina"))
                .andExpect(jsonPath("$.data.profilePhoto").value(startsWith("https://res.cloudinary.test/profile-photos/")));
        mvc.perform(get("/api/instagram/status").header(HttpHeaders.AUTHORIZATION, sesionDe(tatuador)))
                .andExpect(jsonPath("$.data.connected").value(true));
    }

    @Test
    void unStateFalsoOVencidoVuelveAlPerfilConError() throws Exception {
        mvc.perform(get("/api/instagram/callback").param("code", "abc").param("state", "no-es-un-jwt"))
                .andExpect(status().isFound())
                .andExpect(header().string(HttpHeaders.LOCATION, "http://localhost:4200/info?instagram=error"));
        mvc.perform(get("/api/instagram/callback").param("error", "access_denied"))
                .andExpect(header().string(HttpHeaders.LOCATION, "http://localhost:4200/info?instagram=denied"));
    }

    @Test
    void laSincronizacionImportaImagenesYCarruselesYDaDeBajaLoBorradoEnInstagram() throws Exception {
        Long tatuador = crearUsuario("Tatuador");
        String token = vincularInstagram(tatuador);
        String sufijo = token.substring(token.length() - 8);
        Publicacion imagen = new Publicacion("img-" + sufijo, "IMAGE", "Rosa", List.of("https://ig.test/rosa.jpg"), HOY);
        Publicacion carrusel = new Publicacion("car-" + sufijo, "CAROUSEL_ALBUM", "Manga",
                List.of("https://ig.test/1.jpg", "https://ig.test/2.jpg"), HOY);
        Publicacion video = new Publicacion("vid-" + sufijo, "VIDEO", "Proceso", List.of(), HOY);
        instagram.publicar(token, List.of(imagen, carrusel, video));

        mvc.perform(post("/api/instagram/sync").header(HttpHeaders.AUTHORIZATION, sesionDe(tatuador)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nuevos").value(2))
                .andExpect(jsonPath("$.data.omitidos").value(1));
        mvc.perform(get("/api/trabajos/tatuador/" + tatuador))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[?(@.instagramMediaId == 'car-" + sufijo + "')].fotos.length()").value(2));

        instagram.publicar(token, List.of(carrusel));
        mvc.perform(post("/api/instagram/sync").header(HttpHeaders.AUTHORIZATION, sesionDe(tatuador)))
                .andExpect(jsonPath("$.data.nuevos").value(0))
                .andExpect(jsonPath("$.data.eliminados").value(1));
        mvc.perform(get("/api/trabajos/tatuador/" + tatuador))
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    void conElTokenVencidoLaSincronizacionDesvinculaLaCuenta() throws Exception {
        Long tatuador = crearUsuario("Tatuador");
        String token = vincularInstagram(tatuador);
        instagram.vencer(token);

        mvc.perform(post("/api/instagram/sync").header(HttpHeaders.AUTHORIZATION, sesionDe(tatuador)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("INSTAGRAM_TOKEN_VENCIDO"));
        assertThat(tokens.findByTatuador_Id(tatuador)).isEmpty();
        mvc.perform(get("/api/users/" + tatuador).header(HttpHeaders.AUTHORIZATION, sesionDe(tatuador)))
                .andExpect(jsonPath("$.data.instagramHandle").doesNotExist())
                .andExpect(jsonPath("$.data.profilePhoto").doesNotExist());
    }

    @Test
    void laTareaProgramadaDesvinculaLosVencidosYRenuevaLosQueVencenPronto() throws Exception {
        Long vencido = crearUsuario("Tatuador");
        vincularInstagram(vencido);
        Long porVencer = crearUsuario("Tatuador");
        String tokenPorVencer = vincularInstagram(porVencer);
        cambiarVencimiento(vencido, HOY.minusHours(1));
        cambiarVencimiento(porVencer, HOY.plusDays(5));
        int renovacionesAntes = instagram.renovaciones();

        sincronizacion.sincronizarTodas();

        assertThat(tokens.findByTatuador_Id(vencido)).isEmpty();
        assertThat(instagram.renovaciones()).isGreaterThan(renovacionesAntes);
        assertThat(tokens.findByTatuador_Id(porVencer)).hasValueSatisfying(token -> {
            assertThat(token.getAccessToken()).isEqualTo(tokenPorVencer + "-renovado");
            assertThat(token.getTokenExpiresAt()).isEqualTo(HOY.plusSeconds(InstagramApiFalsa.SESENTA_DIAS));
        });
    }

    @Test
    void cuandoUnTatuadorIniciaSesionSeSincronizaEnSegundoPlano() throws Exception {
        Long tatuador = crearUsuario("Tatuador");
        String token = vincularInstagram(tatuador);
        String id = "login-" + token.substring(token.length() - 8);
        instagram.publicar(token, List.of(new Publicacion(id, "IMAGE", null, List.of("https://ig.test/x.jpg"), HOY)));

        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"identifier\": \"" + usernameDe(tatuador) + "\", \"password\": \"" + cifrar(CONTRASENA) + "\"}"))
                .andExpect(status().isOk());

        esperarTareasEnSegundoPlano();
        assertThat(jdbc.queryForObject("SELECT count(*) FROM tat_trabajo WHERE instagram_media_id = ?", Long.class, id))
                .isEqualTo(1L);
    }

    @Test
    void alDesvincularSeBorraElTokenYSeConservanLosTrabajos() throws Exception {
        Long tatuador = crearUsuario("Tatuador");
        String token = vincularInstagram(tatuador);
        instagram.publicar(token, List.of(new Publicacion("des-" + token.substring(token.length() - 8), "IMAGE", null,
                List.of("https://ig.test/y.jpg"), HOY)));
        mvc.perform(post("/api/instagram/sync").header(HttpHeaders.AUTHORIZATION, sesionDe(tatuador)));

        mvc.perform(delete("/api/instagram/disconnect").header(HttpHeaders.AUTHORIZATION, sesionDe(tatuador)))
                .andExpect(status().isOk());

        assertThat(tokens.findByTatuador_Id(tatuador)).isEmpty();
        mvc.perform(get("/api/trabajos/tatuador/" + tatuador)).andExpect(jsonPath("$.data.length()").value(1));
    }

    /**
     * Por el repositorio y no por SQL: así el valor pasa por la misma conversión de zona horaria
     * ({@code hibernate.jdbc.time_zone}) con la que lo lee la app, en cualquier equipo.
     */
    private void cambiarVencimiento(Long tatuador, LocalDateTime vencimiento) {
        transacciones.executeWithoutResult(estado ->
                tokens.findByTatuador_Id(tatuador).orElseThrow().setTokenExpiresAt(vencimiento));
    }
}
