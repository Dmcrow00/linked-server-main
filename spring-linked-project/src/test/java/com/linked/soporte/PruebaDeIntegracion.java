package com.linked.soporte;

import com.dmc00.core.security.ClavesRsa;
import com.dmc00.core.swe.repository.SweRolRepository;
import com.dmc00.core.swe.service.AuthService;
import com.dmc00.core.swe.service.SweUsrService;
import com.dmc00.core.test.PostgresContenedor;
import com.linked.dto.UserDto;
import com.linked.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.util.UriComponentsBuilder;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.crypto.Cipher;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.spec.MGF1ParameterSpec;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;

/**
 * Base de los tests de integración: la app completa contra PostgreSQL en Testcontainers, con las
 * migraciones reales y los dobles de {@link DoblesDePrueba}. Todas las clases comparten el mismo
 * contexto (y la misma base): cada test crea sus propios usuarios con nombres únicos.
 * Sin Docker se saltean.
 */
@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Import({PostgresContenedor.class, DoblesDePrueba.class})
public abstract class PruebaDeIntegracion {

    protected static final String CONTRASENA = "Clave-de-prueba-1";

    @Autowired
    protected MockMvc mvc;

    @Autowired
    protected JdbcTemplate jdbc;

    @Autowired
    protected UserService userService;

    @Autowired
    protected SweUsrService sweUsrService;

    @Autowired
    protected AuthService authService;

    @Autowired
    protected SweRolRepository roles;

    @Autowired
    protected ClavesRsa claves;

    /** El pool de {@code @Async}: el login y la vinculación disparan sincronizaciones de Instagram. */
    @Autowired
    protected ThreadPoolTaskExecutor tareasEnSegundoPlano;

    /** Un usuario registrado como lo hace el cliente web, con los roles indicados (por defecto, Cliente). */
    protected Long crearUsuario(String... rolesDelUsuario) {
        String username = "u" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        Long id = userService.register(new UserDto.CreateRequest(username, username + "@linked.test",
                cifrar(CONTRASENA), "Nombre", "Apellido", LocalDate.of(1990, 1, 1),
                "Rosario", -32.95, -60.65, null)).userId();
        if (rolesDelUsuario.length > 0) {
            sweUsrService.updateRoles(id, List.of(rolesDelUsuario).stream()
                    .map(rol -> roles.findByDescription(rol).orElseThrow().getId())
                    .toList());
        }
        return id;
    }

    protected String usernameDe(Long id) {
        return sweUsrService.findById(id).username();
    }

    /** Header {@code Authorization} con un JWT de sesión del usuario. */
    protected String sesionDe(Long id) {
        return "Bearer " + authService.issueToken(id).token();
    }

    /**
     * Vincula Instagram como lo haría el navegador: inicio del OAuth, autorización en Instagram
     * (falso) y callback. Devuelve el token de acceso que quedó guardado.
     */
    protected String vincularInstagram(Long tatuador) throws Exception {
        String autorizacion = mvc.perform(get("/api/instagram/auth").header(HttpHeaders.AUTHORIZATION, sesionDe(tatuador)))
                .andReturn().getResponse().getHeader(HttpHeaders.LOCATION);
        String state = UriComponentsBuilder.fromUriString(autorizacion).build().getQueryParams().getFirst("state");
        String code = UUID.randomUUID().toString().replace("-", "");
        mvc.perform(get("/api/instagram/callback").param("code", code).param("state", state))
                .andExpect(header().string(HttpHeaders.LOCATION, "http://localhost:4200/info?instagram=success"));
        esperarTareasEnSegundoPlano();
        return "token-" + code;
    }

    /** Espera a que terminen las tareas {@code @Async} pendientes, para que no se crucen con el test. */
    protected void esperarTareasEnSegundoPlano() {
        await().atMost(Duration.ofSeconds(10)).until(() -> tareasEnSegundoPlano.getActiveCount() == 0
                && tareasEnSegundoPlano.getThreadPoolExecutor().getQueue().isEmpty());
    }

    /** Lo mismo que hace el cliente Angular con {@code crypto.subtle.encrypt({name: 'RSA-OAEP'}, ...)}. */
    protected String cifrar(String texto) {
        try {
            Cipher cifrador = Cipher.getInstance("RSA/ECB/OAEPPadding");
            cifrador.init(Cipher.ENCRYPT_MODE, claves.publica(),
                    new OAEPParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256, PSource.PSpecified.DEFAULT));
            return Base64.getEncoder().encodeToString(cifrador.doFinal(texto.getBytes(StandardCharsets.UTF_8)));
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
