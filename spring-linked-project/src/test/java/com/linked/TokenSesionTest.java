package com.linked;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.linked.dto.AuthDto;
import com.linked.dto.UserDto;
import com.linked.model.entity.SweRolapl;
import com.linked.model.entity.SweUsrapl;
import com.linked.repository.SweRolRepository;
import com.linked.repository.SweUsrRepository;
import com.linked.security.JwtService;
import com.linked.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.crypto.Cipher;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.security.spec.MGF1ParameterSpec;
import java.time.LocalDate;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * El JWT de sesión lleva {@code id} y {@code rol} para que el cliente arme la interfaz, pero la
 * autorización del backend no los usa: los roles se leen de la base en cada request.
 */
@SpringBootTest
@ActiveProfiles("test")
class TokenSesionTest {

    private static final String PASSWORD = "Clave-de-prueba-1";

    @Autowired private WebApplicationContext context;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private PublicKey rsaPublicKey;
    @Autowired private JwtService jwtService;
    @Autowired private UserService userService;
    @Autowired private SweRolRepository rolRepository;
    @Autowired private SweUsrRepository usrRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        // En H2 no corre Flyway, así que no están los roles semilla de V1.
        List.of("Administrador", "Moderador", "Tatuador", "Cliente").forEach(this::rol);
    }

    @Test
    void elTokenDelRegistroTraeElIdYElRolCliente() {
        AuthDto.TokenResponse respuesta = registrar("token_registro");

        assertThat(claim(respuesta.token(), "id", Long.class))
                .isEqualTo(respuesta.userId());
        assertThat(claim(respuesta.token(), "rol", String.class))
                .isEqualTo("Cliente");
    }

    @Test
    void conVariosRolesElTokenInformaElDeMayorJerarquiaYElBackendLoAutoriza() throws Exception {
        Long id = registrar("token_admin").userId();
        userService.updateRoles(id, new UserDto.UpdateRolesRequest(
                List.of(rol("Cliente").getId(), rol("Administrador").getId())));

        String token = login("token_admin");

        assertThat(claim(token, "id", Long.class)).isEqualTo(id);
        assertThat(claim(token, "rol", String.class)).isEqualTo("Administrador");
        mockMvc.perform(get("/api/users").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void unRolAdulteradoEnElTokenNoDaPermisos() throws Exception {
        Long id = registrar("token_cliente").userId();
        SweUsrapl cliente = usrRepository.findById(id).orElseThrow();
        String token = jwtService.generateToken(Map.of("id", id, "rol", "Administrador"), cliente);

        mockMvc.perform(get("/api/users").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    private AuthDto.TokenResponse registrar(String username) {
        return userService.register(new UserDto.CreateRequest(username, username + "@linked.test",
                cifrar(PASSWORD), null, null, LocalDate.of(1990, 1, 1), null, null, null, null));
    }

    private String login(String username) throws Exception {
        String body = objectMapper.writeValueAsString(
                new AuthDto.LoginRequest(username, cifrar(PASSWORD), null));
        String respuesta = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode json = objectMapper.readTree(respuesta);
        return json.path("data").path("token").asText();
    }

    private <T> T claim(String token, String nombre, Class<T> tipo) {
        return jwtService.extractClaim(token, c -> c.get(nombre, tipo));
    }

    /** Cifra como el cliente web: RSA-OAEP con SHA-256 en el digest y en MGF1. */
    private String cifrar(String texto) {
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPPadding");
            cipher.init(Cipher.ENCRYPT_MODE, rsaPublicKey, new OAEPParameterSpec(
                    "SHA-256", "MGF1", MGF1ParameterSpec.SHA256, PSource.PSpecified.DEFAULT));
            return Base64.getEncoder().encodeToString(cipher.doFinal(texto.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private SweRolapl rol(String descripcion) {
        return rolRepository.findByDescription(descripcion).orElseGet(() -> {
            SweRolapl rol = new SweRolapl();
            rol.setDescription(descripcion);
            return rolRepository.save(rol);
        });
    }
}
