package com.linked;

import com.jayway.jsonpath.JsonPath;
import com.linked.soporte.DoblesDePrueba;
import com.linked.soporte.PruebaDeIntegracion;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Perfil, registro de tatuadores y recuperación de contraseña ({@code /api/users}). */
class UsuariosIT extends PruebaDeIntegracion {

    @Autowired
    DoblesDePrueba.CorreoFalso correo;

    @Test
    void cadaUsuarioEditaSuPerfilYNoElDeOtro() throws Exception {
        Long ana = crearUsuario();
        Long beto = crearUsuario();

        mvc.perform(put("/api/users/" + ana).header(HttpHeaders.AUTHORIZATION, sesionDe(ana))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"realname": "Ana", "estudio": "Tinta Sur", "localidad": "Mendoza", "lat": -32.89, "lng": -68.83}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.realname").value("Ana"))
                .andExpect(jsonPath("$.data.estudio").value("Tinta Sur"))
                .andExpect(jsonPath("$.data.localidad").value("Mendoza"));

        mvc.perform(put("/api/users/" + beto).header(HttpHeaders.AUTHORIZATION, sesionDe(ana))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"realname\": \"Intruso\"}"))
                .andExpect(status().isForbidden());
    }

    /** Reemplaza al {@code PUT /api/users/updateUser/:id} de Node: el administrador edita a otro usuario. */
    @Test
    void elAdministradorEditaElPerfilDeCualquierUsuario() throws Exception {
        Long admin = crearUsuario("Administrador");
        Long usuario = crearUsuario();

        mvc.perform(put("/api/users/" + usuario).header(HttpHeaders.AUTHORIZATION, sesionDe(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"surname": "Corregido", "fechaInicioActividad": "2015-03-01"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.surname").value("Corregido"))
                .andExpect(jsonPath("$.data.fechaInicioActividad").value("2015-03-01"));
    }

    @Test
    void laFotoSincronizadaConInstagramNoSeCambiaAMano() throws Exception {
        Long tatuador = crearUsuario("Tatuador");
        MockMultipartFile foto = new MockMultipartFile("file", "yo.jpg", "image/jpeg", new byte[] {1, 2, 3});

        mvc.perform(multipart("/api/users/" + tatuador + "/profile-photo").file(foto)
                        .with(pedido -> { pedido.setMethod("PATCH"); return pedido; })
                        .header(HttpHeaders.AUTHORIZATION, sesionDe(tatuador)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.profilePhoto").value(startsWith("https://res.cloudinary.test/")));

        vincularInstagram(tatuador);

        mvc.perform(multipart("/api/users/" + tatuador + "/profile-photo").file(foto)
                        .with(pedido -> { pedido.setMethod("PATCH"); return pedido; })
                        .header(HttpHeaders.AUTHORIZATION, sesionDe(tatuador)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("FOTO_SINCRONIZADA_CON_INSTAGRAM"));
    }

    @Test
    void elPerfilPublicoDeUnTatuadorNoExponeDatosPersonales() throws Exception {
        Long tatuador = crearUsuario("Tatuador");
        Long cliente = crearUsuario();

        mvc.perform(get("/api/users/" + tatuador + "/publico"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").exists())
                .andExpect(jsonPath("$.data.email").doesNotExist())
                .andExpect(jsonPath("$.data.birthDate").doesNotExist());
        mvc.perform(get("/api/users/" + cliente + "/publico"))
                .andExpect(status().isNotFound());
    }

    @Test
    void unTatuadorSeRegistraComoClienteYAlAprobarloPasaATatuadorConSusEspecialidades() throws Exception {
        Long admin = crearUsuario("Administrador");
        Long realismo = crearEspecialidad(admin, "Realismo " + UUID.randomUUID().toString().substring(0, 8));
        String username = "tat" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);

        String registro = mvc.perform(post("/api/users/register-tatuador").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username": "%s", "email": "%s@linked.test", "password": "%s", "birth_date": "1992-02-02",
                                 "estudio": "Aguja Fina", "especialidadIds": [%d], "localidad": "Rosario", "lat": -32.95, "lng": -60.65}
                                """.formatted(username, username, cifrar(CONTRASENA), realismo)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long tatuador = ((Number) JsonPath.read(registro, "$.data.userId")).longValue();
        assertThat(sweUsrService.findById(tatuador).roles()).extracting("description").containsExactly("Cliente");

        String solicitudes = mvc.perform(get("/api/solicitudes-tatuador").header(HttpHeaders.AUTHORIZATION, sesionDe(admin)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        List<Integer> ids = JsonPath.read(solicitudes, "$.data[?(@.user.id == " + tatuador + ")].id");
        mvc.perform(put("/api/solicitudes-tatuador/" + ids.getFirst() + "/aprobar").header(HttpHeaders.AUTHORIZATION, sesionDe(admin)))
                .andExpect(status().isOk());

        assertThat(sweUsrService.findById(tatuador).roles()).extracting("description").containsExactly("Tatuador");
        mvc.perform(get("/api/tatuador/" + tatuador + "/especialidades").header(HttpHeaders.AUTHORIZATION, sesionDe(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[*].id").value(hasItem(realismo.intValue())));
        // Sin sesión la búsqueda no muestra el contacto (Instagram).
        String busqueda = mvc.perform(get("/api/busqueda/tatuadores-ubicacion"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        List<String> estudios = JsonPath.read(busqueda, "$.data[?(@.id == " + tatuador + ")].estudio");
        List<Object> handles = JsonPath.read(busqueda, "$.data[*].instagramHandle");
        assertThat(estudios).containsExactly("Aguja Fina");
        assertThat(handles).containsOnlyNulls();
    }

    @Test
    void laRecuperacionDeContrasenaUsaElTokenDelMail() throws Exception {
        Long id = crearUsuario();
        String email = sweUsrService.findById(id).email();

        mvc.perform(post("/api/users/forgot-password").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"" + email + "\"}"))
                .andExpect(status().isOk());
        String token = correo.recuperaciones.stream()
                .filter(recuperacion -> recuperacion.email().equals(email))
                .findFirst().orElseThrow().token();

        mvc.perform(post("/api/users/reset-password").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\": \"" + token + "\", \"newPassword\": \"" + cifrar("Clave-nueva-2") + "\"}"))
                .andExpect(status().isOk());
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"identifier\": \"" + email + "\", \"password\": \"" + cifrar("Clave-nueva-2") + "\"}"))
                .andExpect(status().isOk());
    }

    private Long crearEspecialidad(Long admin, String nombre) throws Exception {
        String respuesta = mvc.perform(post("/api/especialidades").header(HttpHeaders.AUTHORIZATION, sesionDe(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\": \"" + nombre + "\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(respuesta, "$.data.id")).longValue();
    }
}
