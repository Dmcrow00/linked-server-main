package com.linked.config;

import com.dmc00.core.swe.repository.SweUsrRepository;
import com.dmc00.core.swe.repository.SweUsrauthRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Solo en el perfil dev: le pone al admin semilla la contraseña de {@code ADMIN_DEV_PASSWORD}.
 * <p>
 * V18 deja al admin que siembra V1 sin contraseña utilizable (los hashes que cargó V1 eran
 * conocidos y el repositorio es público). En desarrollo hace falta poder entrar: este runner le
 * asigna la contraseña local, y solo si sigue sin contraseña, así nunca pisa una que ya se cambió.
 * Sin {@code ADMIN_DEV_PASSWORD} no hace nada.
 */
@Slf4j
@Component
@Profile("dev")
public class AdminSemillaDevRunner implements ApplicationRunner {

    /** Lo que deja V18 en lugar del hash. */
    static final String SIN_CONTRASENA = "SIN-CONTRASENA";
    static final String USUARIO = "admin";

    private final SweUsrRepository usuarios;
    private final SweUsrauthRepository credenciales;
    private final PasswordEncoder codificador;
    private final String contrasena;

    public AdminSemillaDevRunner(SweUsrRepository usuarios, SweUsrauthRepository credenciales,
                                 PasswordEncoder codificador,
                                 @Value("${app.dev.admin-password:}") String contrasena) {
        this.usuarios = usuarios;
        this.credenciales = credenciales;
        this.codificador = codificador;
        this.contrasena = contrasena;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (contrasena == null || contrasena.isBlank()) {
            return;
        }
        usuarios.findByUsername(USUARIO)
                .flatMap(admin -> credenciales.findByUsrapl_Id(admin.getId()))
                .filter(credencial -> SIN_CONTRASENA.equals(credencial.getPassword()))
                .ifPresent(credencial -> {
                    credencial.setPassword(codificador.encode(contrasena));
                    log.warn("Perfil dev: el usuario '{}' entra con la contraseña de ADMIN_DEV_PASSWORD.", USUARIO);
                });
    }
}
