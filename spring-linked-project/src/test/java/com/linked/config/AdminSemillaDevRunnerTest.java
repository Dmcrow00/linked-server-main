package com.linked.config;

import com.dmc00.core.swe.model.entity.SweUsrapl;
import com.dmc00.core.swe.model.entity.SweUsrauth;
import com.dmc00.core.swe.repository.SweUsrRepository;
import com.dmc00.core.swe.repository.SweUsrauthRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AdminSemillaDevRunnerTest {

    private final SweUsrRepository usuarios = mock(SweUsrRepository.class);
    private final SweUsrauthRepository credenciales = mock(SweUsrauthRepository.class);
    private final PasswordEncoder codificador = new BCryptPasswordEncoder(4);
    private SweUsrauth credencial;

    @BeforeEach
    void adminSembrado() {
        SweUsrapl admin = new SweUsrapl();
        admin.setId(1L);
        admin.setUsername("admin");
        credencial = new SweUsrauth(admin, AdminSemillaDevRunner.SIN_CONTRASENA);
        when(usuarios.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(credenciales.findByUsrapl_Id(1L)).thenReturn(Optional.of(credencial));
    }

    @Test
    void leAsignaLaContrasenaLocalAlAdminSinContrasena() {
        runner("clave-local").run(new DefaultApplicationArguments());

        assertThat(codificador.matches("clave-local", credencial.getPassword())).isTrue();
    }

    @Test
    void noPisaUnaContrasenaQueYaSeCambio() {
        credencial.setPassword("$2a$10$unaQueYaEligioAlguien");

        runner("clave-local").run(new DefaultApplicationArguments());

        assertThat(credencial.getPassword()).isEqualTo("$2a$10$unaQueYaEligioAlguien");
    }

    @Test
    void sinContrasenaLocalNoHaceNada() {
        runner("").run(new DefaultApplicationArguments());

        assertThat(credencial.getPassword()).isEqualTo(AdminSemillaDevRunner.SIN_CONTRASENA);
    }

    private AdminSemillaDevRunner runner(String contrasena) {
        return new AdminSemillaDevRunner(usuarios, credenciales, codificador, contrasena);
    }
}
