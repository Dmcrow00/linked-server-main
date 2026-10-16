package com.linked.soporte;

import com.dmc00.core.swe.dto.SweUsrDto;
import com.linked.integration.AlmacenDeImagenes;
import com.linked.service.EmailService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/** Reemplazos de las integraciones externas: ningún test sale a la red. */
@TestConfiguration(proxyBeanMethods = false)
public class DoblesDePrueba {

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
