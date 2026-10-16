package com.linked;

import com.dmc00.core.test.PostgresContenedor;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Las migraciones de linked sobre bases que ya tenían datos: lo que no prueban los demás IT, que
 * arrancan de una base vacía. Cada caso migra hasta una versión, carga datos como estaban en ese
 * momento y sigue. Usa Flyway directo, sin Spring; cada caso en una base nueva del mismo contenedor.
 */
@Testcontainers(disabledWithoutDocker = true)
class MigracionesIT {

    @Container
    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer(DockerImageName.parse(PostgresContenedor.IMAGEN));

    private String base;
    private JdbcTemplate jdbc;

    @BeforeEach
    void baseNueva() {
        base = "migraciones_" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
        new JdbcTemplate(fuente(POSTGRES.getDatabaseName())).execute("CREATE DATABASE " + base);
        jdbc = new JdbcTemplate(fuente(base));
    }

    @AfterEach
    void borrarBase() {
        new JdbcTemplate(fuente(POSTGRES.getDatabaseName())).execute("DROP DATABASE IF EXISTS " + base + " WITH (FORCE)");
    }

    /** Una base heredada del backend Node trae los datos del usuario en las columnas viejas. */
    @Test
    void v11CopiaLasColumnasDeNodeSiExisten() {
        migrarHasta("10");
        jdbc.execute("""
                ALTER TABLE swe_usrapl ADD COLUMN nombre VARCHAR(25), ADD COLUMN apellido VARCHAR(25),
                    ADD COLUMN nombre_usuario VARCHAR(25), ADD COLUMN fecha_nacimiento DATE, ADD COLUMN foto_perfil TEXT;
                UPDATE swe_usrapl SET realname = NULL, surname = NULL, nombre = 'Ana', apellido = 'Paz',
                    fecha_nacimiento = '1985-05-20', foto_perfil = 'https://img.test/ana.jpg'
                WHERE username = 'admin';
                """);

        migrarTodo();

        Map<String, Object> admin = jdbc.queryForMap(
                "SELECT realname, surname, birth_date::text AS birth_date, profile_photo FROM swe_usrapl WHERE username = 'admin'");
        assertThat(admin).containsEntry("realname", "Ana").containsEntry("surname", "Paz")
                .containsEntry("birth_date", "1985-05-20").containsEntry("profile_photo", "https://img.test/ana.jpg");
    }

    @Test
    void v18DejaSinContrasenaSoloAlAdminConUnHashSembrado() {
        migrarHasta("17");
        jdbc.update("UPDATE swe_usrauth SET password = '$2a$10$RSHX1.3YoGJuZN.xWfGCweRMEJ6EJv/aUBmFpDNv.LxKZ0cWQHMiC'");
        Long otro = crearUsuario("otra");
        jdbc.update("INSERT INTO swe_usrauth (id_usrapl, password) VALUES (?, '$2a$10$otroHashQueNoSeToca')", otro);

        migrarTodo();

        assertThat(contrasenaDe("admin")).isEqualTo("SIN-CONTRASENA");
        assertThat(contrasenaDe("otra")).isEqualTo("$2a$10$otroHashQueNoSeToca");
    }

    @Test
    void v19CreaUnPerfilPorUsuarioConLosDatosDeLinked() {
        migrarHasta("18");
        Long tatuadora = crearUsuario("tatuadora");
        jdbc.update("""
                UPDATE swe_usrapl SET estudio = 'Tinta Sur', fecha_inicio_actividad = '2015-01-01', localidad = 'Rosario',
                    lat = -32.95, lng = -60.65, instagram_handle = 'tinta.sur' WHERE id = ?
                """, tatuadora);

        migrarTodo();

        assertThat(jdbc.queryForObject("SELECT count(*) FROM tat_perfil_usuario", Long.class))
                .isEqualTo(jdbc.queryForObject("SELECT count(*) FROM swe_usrapl", Long.class));
        Map<String, Object> perfil = jdbc.queryForMap(
                "SELECT estudio, localidad, lat, instagram_handle FROM tat_perfil_usuario WHERE id_usrapl = ?", tatuadora);
        assertThat(perfil).containsEntry("estudio", "Tinta Sur").containsEntry("localidad", "Rosario")
                .containsEntry("lat", -32.95).containsEntry("instagram_handle", "tinta.sur");
    }

    @Test
    void v20RetiraLasRutasLocalesDeUploadsYLasRegistra() {
        migrarHasta("19");
        Long cliente = crearUsuario("cliente_node");
        jdbc.update("UPDATE swe_usrapl SET profile_photo = '/uploads/users/cliente.png' WHERE id = ?", cliente);
        Long turno = jdbc.queryForObject("""
                INSERT INTO tat_turno_sesion (id_cliente, fecha_hora_inicio, fecha_hora_fin)
                VALUES (?, '2025-01-01 10:00', '2025-01-01 11:00') RETURNING id
                """, Long.class, cliente);
        jdbc.update("INSERT INTO tat_imagen_ref (image_path, id_turno_sesion) VALUES ('/uploads/turnos/ref.png', ?)", turno);
        jdbc.update("INSERT INTO tat_imagen_ref (image_path, id_turno_sesion) VALUES ('https://res.cloudinary.com/x.png', ?)", turno);

        migrarTodo();

        assertThat(jdbc.queryForObject("SELECT profile_photo FROM swe_usrapl WHERE id = ?", String.class, cliente)).isNull();
        assertThat(jdbc.queryForList("SELECT image_path FROM tat_imagen_ref WHERE id_turno_sesion = ?", String.class, turno))
                .containsExactly("https://res.cloudinary.com/x.png");
        assertThat(jdbc.queryForList("SELECT tabla || ':' || ruta FROM tat_ruta_local_retirada ORDER BY id", String.class))
                .containsExactly("swe_usrapl:/uploads/users/cliente.png", "tat_imagen_ref:/uploads/turnos/ref.png");
    }

    private Long crearUsuario(String username) {
        return jdbc.queryForObject("""
                INSERT INTO swe_usrapl (username, email, birth_date) VALUES (?, ?, '1990-01-01') RETURNING id
                """, Long.class, username, username + "@linked.test");
    }

    private String contrasenaDe(String username) {
        return jdbc.queryForObject("""
                SELECT a.password FROM swe_usrauth a JOIN swe_usrapl u ON u.id = a.id_usrapl WHERE u.username = ?
                """, String.class, username);
    }

    private void migrarHasta(String version) {
        flyway().target(version).load().migrate();
    }

    private void migrarTodo() {
        flyway().load().migrate();
    }

    private FluentConfiguration flyway() {
        return Flyway.configure().dataSource(fuente(base)).locations("classpath:db/migration/linked");
    }

    private static DriverManagerDataSource fuente(String nombreDeLaBase) {
        String url = POSTGRES.getJdbcUrl().replace("/" + POSTGRES.getDatabaseName(), "/" + nombreDeLaBase);
        return new DriverManagerDataSource(url, POSTGRES.getUsername(), POSTGRES.getPassword());
    }
}
