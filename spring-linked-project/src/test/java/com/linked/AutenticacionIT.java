package com.linked;

import com.dmc00.core.security.JwtService;
import com.dmc00.core.swe.model.entity.SweUsrapl;
import com.dmc00.core.swe.repository.SweUsrRepository;
import com.jayway.jsonpath.JsonPath;
import com.linked.service.InstagramService;
import com.linked.soporte.PruebaDeIntegracion;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Login, registro y JWT con core-security y core-swe sobre la base de linked. */
class AutenticacionIT extends PruebaDeIntegracion {

    @Autowired
    JwtService jwt;

    @Autowired
    SweUsrRepository usuarios;

    @Autowired
    InstagramService instagram;

    /**
     * Los usuarios creados en el backend Node tienen hash Argon2id (paquete argon2 de Node, con sus
     * parámetros por defecto) y siguen entrando: los valida el PasswordEncoder de core-security.
     */
    @Test
    void unUsuarioTraidoDeNodeConHashArgon2IniciaSesion() throws Exception {
        String hashDeNode = new Argon2PasswordEncoder(16, 32, 4, 65536, 3).encode("Clave-de-node-1");
        assertThat(hashDeNode).startsWith("$argon2id$v=19$m=65536,t=3,p=4$");
        jdbc.update("INSERT INTO swe_usrapl (username, email, birth_date) VALUES ('de_node', 'de_node@linked.test', '1995-03-01')");
        jdbc.update("INSERT INTO swe_usrauth (id_usrapl, password) SELECT id, ? FROM swe_usrapl WHERE username = 'de_node'",
                hashDeNode);
        jdbc.update("""
                INSERT INTO swe_usrrolapl (id_usrapl, id_rolapl)
                SELECT u.id, r.id FROM swe_usrapl u, swe_rolapl r WHERE u.username = 'de_node' AND r.description = 'Cliente'
                """);

        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(login("de_node", "Clave-de-node-1")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("de_node"));
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(login("de_node", "otra-clave")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void elRegistroCreaElPerfilYDevuelveUnTokenConIdYRolCliente() throws Exception {
        String respuesta = mvc.perform(post("/api/users/register").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username": "nueva_clienta", "email": "nueva_clienta@linked.test",
                                 "password": "%s", "birth_date": "1998-06-01",
                                 "localidad": "Córdoba", "lat": -31.42, "lng": -64.18}
                                """.formatted(cifrar(CONTRASENA))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long id = ((Number) JsonPath.read(respuesta, "$.data.userId")).longValue();
        String token = JsonPath.read(respuesta, "$.data.token");
        assertThat(claim(token, "id", Long.class)).isEqualTo(id);
        assertThat(claim(token, "rol", String.class)).isEqualTo("Cliente");
        assertThat(jdbc.queryForObject("SELECT localidad FROM tat_perfil_usuario WHERE id_usrapl = ?", String.class, id))
                .isEqualTo("Córdoba");
    }

    @Test
    void conVariosRolesElTokenInformaElDeMayorJerarquiaYElBackendLoAutoriza() throws Exception {
        Long id = crearUsuario("Cliente", "Administrador");

        String respuesta = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(login(usernameDe(id), CONTRASENA)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String token = JsonPath.read(respuesta, "$.data.token");

        assertThat(claim(token, "rol", String.class)).isEqualTo("Administrador");
        mvc.perform(get("/api/users").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void unRolAdulteradoEnElTokenNoDaPermisos() throws Exception {
        Long id = crearUsuario();
        SweUsrapl cliente = usuarios.findByUsernameForAuth(usernameDe(id)).orElseThrow();
        String token = jwt.generateToken(Map.of("id", id, "rol", "Administrador", "roles", List.of("Administrador")), cliente);

        mvc.perform(get("/api/users").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void elAdminSembradoNoTieneUnaContrasenaUtilizable() throws Exception {
        assertThat(jdbc.queryForObject("""
                SELECT a.password FROM swe_usrauth a JOIN swe_usrapl u ON u.id = a.id_usrapl WHERE u.username = 'admin'
                """, String.class)).isEqualTo("SIN-CONTRASENA");
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(login("admin", "SIN-CONTRASENA")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void elTokenPorQuerySoloSirveParaIniciarElOAuthDeInstagram() throws Exception {
        Long tatuador = crearUsuario("Tatuador");
        String token = authService.issueToken(tatuador).token();

        mvc.perform(get("/api/instagram/auth").param("token", token))
                .andExpect(status().isFound())
                .andExpect(header().string(HttpHeaders.LOCATION,
                        startsWith("https://instagram.test/oauth/authorize?state=")));
        mvc.perform(get("/api/users/" + tatuador).param("token", token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void elStateDelOAuthNoSirveComoTokenDeSesion() throws Exception {
        Long tatuador = crearUsuario("Tatuador");
        String state = UriComponentsBuilder.fromUriString(instagram.initiateOAuth(tatuador))
                .build().getQueryParams().getFirst("state");

        mvc.perform(get("/api/users/" + tatuador).header(HttpHeaders.AUTHORIZATION, "Bearer " + state))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void elMenuDeCoreSweResponde() throws Exception {
        Long id = crearUsuario();
        mvc.perform(get("/api/swe/menu").header(HttpHeaders.AUTHORIZATION, sesionDe(id)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    private <T> T claim(String token, String nombre, Class<T> tipo) {
        return jwt.extractClaim(token, claims -> claims.get(nombre, tipo));
    }

    private String login(String usuario, String contrasena) {
        return """
                {"identifier": "%s", "password": "%s"}
                """.formatted(usuario, cifrar(contrasena));
    }
}
