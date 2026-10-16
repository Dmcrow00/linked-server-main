package com.linked.soporte;

import com.dmc00.core.swe.dto.SweUsrDto;
import com.linked.integration.AlmacenDeImagenes;
import com.linked.service.EmailService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.web.multipart.MultipartFile;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/** Reemplazos de las integraciones externas: ningún test sale a la red. */
@TestConfiguration(proxyBeanMethods = false)
public class DoblesDePrueba {

    /** Hora fija de los tests: {@link #AHORA} en la zona de la app, sin importar la del equipo que corre los tests. */
    public static final ZoneId ZONA = ZoneId.of("America/Argentina/Buenos_Aires");
    public static final LocalDateTime AHORA = LocalDateTime.of(2026, 10, 15, 12, 0);

    /** Los vencimientos (tokens de Instagram, recuperación de contraseña) se calculan con este reloj. */
    @Bean
    @Primary
    public Clock relojDePrueba() {
        return Clock.fixed(AHORA.atZone(ZONA).toInstant(), ZONA);
    }

    @Bean
    @Primary
    public InstagramApiFalsa instagramApiFalsa() {
        return new InstagramApiFalsa();
    }

    @Bean
    @Primary
    public AlmacenDeImagenes almacenDeImagenesFalso() {
        AtomicInteger subidas = new AtomicInteger();
        return new AlmacenDeImagenes() {
            @Override
            public String subir(MultipartFile archivo, String carpeta) {
                return "https://res.cloudinary.test/" + carpeta + "/" + subidas.incrementAndGet();
            }

            @Override
            public String subir(byte[] imagen, String carpeta) {
                return "https://res.cloudinary.test/" + carpeta + "/" + subidas.incrementAndGet();
            }
        };
    }

    @Bean
    @Primary
    public CorreoFalso correoFalso() {
        return new CorreoFalso();
    }

    /** Guarda los mails de recuperación en lugar de mandarlos. */
    public static class CorreoFalso implements EmailService {

        public final List<SweUsrDto.PasswordReset> recuperaciones = new CopyOnWriteArrayList<>();

        @Override
        public void enviarRecuperacionDeContrasena(SweUsrDto.PasswordReset recuperacion) {
            recuperaciones.add(recuperacion);
        }
    }
}
