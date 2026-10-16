package com.linked;

import com.linked.dto.AuthDto;
import com.linked.security.JwtService;
import com.linked.service.AuthService;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import javax.crypto.Cipher;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.security.spec.MGF1ParameterSpec;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Base64;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Corre las migraciones Flyway sobre PostgreSQL y levanta la aplicación con
 * {@code ddl-auto=validate}: lo que los demás tests no cubren porque usan H2 sin Flyway.
 * <p>
 * Necesita un servidor donde se puedan crear bases. Se activa con {@code LINKED_TEST_PG_URL}
 * (una base cualquiera del servidor, p. ej. {@code jdbc:postgresql://[::1]:5432/postgres}),
 * {@code LINKED_TEST_PG_USER} y {@code LINKED_TEST_PG_PASSWORD}. Cada caso usa una base nueva
 * que se borra al terminar.
 */
@EnabledIfEnvironmentVariable(named = "LINKED_TEST_PG_URL", matches = ".+")
class MigracionesPostgresTest {

    private static final String URL_SERVIDOR = System.getenv("LINKED_TEST_PG_URL");
    private static final String USUARIO = System.getenv().getOrDefault("LINKED_TEST_PG_USER", "postgres");
    private static final String PASSWORD = System.getenv().getOrDefault("LINKED_TEST_PG_PASSWORD", "");

    /** Contraseña documentada del admin semilla (V1). */
    private static final String PASSWORD_ADMIN = "Admin@Linked2024";

    @Test
    void enUnaBaseVaciaLasMigracionesDejanUnEsquemaQueValidaYElAdminPuedeEntrar() throws Exception {
        String base = crearBase();
        try {
            flyway(base).load().migrate();

            try (ConfigurableApplicationContext app = levantarAplicacion(base)) {
                String password = cifrar(app.getBean(PublicKey.class), PASSWORD_ADMIN);
                AuthDto.TokenResponse respuesta = app.getBean(AuthService.class)
                        .login(new AuthDto.LoginRequest("admin", password, null));

                JwtService jwt = app.getBean(JwtService.class);
                Long id = jwt.extractClaim(respuesta.token(), c -> c.get("id", Long.class));
                String rol = jwt.extractClaim(respuesta.token(), c -> c.get("rol", String.class));
                assertThat(id).isEqualTo(respuesta.userId());
                assertThat(rol).isEqualTo("Administrador");
            }
        } finally {
            borrarBase(base);
        }
    }

    /** Una base heredada del backend Node trae los datos del usuario en las columnas viejas. */
    @Test
    void enUnaBaseHeredadaDeNodeV11CopiaLasColumnasViejas() throws Exception {
        String base = crearBase();
        try {
            flyway(base).target("10").load().migrate();
            ejecutar(base, """
                    ALTER TABLE swe_usrapl ADD COLUMN nombre VARCHAR(25), ADD COLUMN apellido VARCHAR(25),
                        ADD COLUMN nombre_usuario VARCHAR(25), ADD COLUMN fecha_nacimiento DATE,
                        ADD COLUMN foto_perfil TEXT;
                    UPDATE swe_usrapl SET realname = NULL, surname = NULL, nombre = 'Ana', apellido = 'Paz',
                        fecha_nacimiento = '1985-05-20', foto_perfil = 'https://img.test/ana.jpg'
                    WHERE username = 'admin';
                    """);

            flyway(base).load().migrate();

            try (Connection con = conectar(base); Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery("SELECT realname, surname, birth_date, profile_photo "
                         + "FROM swe_usrapl WHERE username = 'admin'")) {
                assertThat(rs.next()).isTrue();
                assertThat(rs.getString("realname")).isEqualTo("Ana");
                assertThat(rs.getString("surname")).isEqualTo("Paz");
                assertThat(rs.getString("birth_date")).isEqualTo("1985-05-20");
                assertThat(rs.getString("profile_photo")).isEqualTo("https://img.test/ana.jpg");
            }
        } finally {
            borrarBase(base);
        }
    }

    private FluentConfiguration flyway(String base) {
        return Flyway.configure()
                .dataSource(urlDe(base), USUARIO, PASSWORD)
                .locations("classpath:db/migration/linked");
    }

    private ConfigurableApplicationContext levantarAplicacion(String base) {
        // Como argumentos de línea de comandos para que pisen la configuración H2 del perfil test.
        return new SpringApplicationBuilder(LinkedApplication.class)
                .profiles("test")
                .run("--spring.datasource.url=" + urlDe(base),
                        "--spring.datasource.username=" + USUARIO,
                        "--spring.datasource.password=" + PASSWORD,
                        "--spring.datasource.driver-class-name=org.postgresql.Driver",
                        "--spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect",
                        "--spring.jpa.hibernate.ddl-auto=validate",
                        "--spring.flyway.enabled=true",
                        "--server.port=0");
    }

    private String crearBase() throws Exception {
        String base = "linked_migtest_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        try (Connection con = DriverManager.getConnection(URL_SERVIDOR, USUARIO, PASSWORD);
             Statement st = con.createStatement()) {
            st.execute("CREATE DATABASE " + base);
        }
        return base;
    }

    private void borrarBase(String base) throws Exception {
        try (Connection con = DriverManager.getConnection(URL_SERVIDOR, USUARIO, PASSWORD);
             Statement st = con.createStatement()) {
            st.execute("DROP DATABASE IF EXISTS " + base + " WITH (FORCE)");
        }
    }

    private void ejecutar(String base, String sql) throws Exception {
        try (Connection con = conectar(base); Statement st = con.createStatement()) {
            st.execute(sql);
        }
    }

    private Connection conectar(String base) throws Exception {
        return DriverManager.getConnection(urlDe(base), USUARIO, PASSWORD);
    }

    private String urlDe(String base) {
        return URL_SERVIDOR.substring(0, URL_SERVIDOR.lastIndexOf('/') + 1) + base;
    }

    private String cifrar(PublicKey clave, String texto) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPPadding");
        cipher.init(Cipher.ENCRYPT_MODE, clave, new OAEPParameterSpec(
                "SHA-256", "MGF1", MGF1ParameterSpec.SHA256, PSource.PSpecified.DEFAULT));
        return Base64.getEncoder().encodeToString(cipher.doFinal(texto.getBytes(StandardCharsets.UTF_8)));
    }
}
