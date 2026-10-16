package com.linked;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.Clock;

/**
 * Punto de entrada de la API de LINKED.
 * <p>
 * {@code @EnableScheduling} habilita la sincronización periódica de Instagram y
 * {@code @EnableAsync} el envío de mails y las sincronizaciones en segundo plano.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
@EnableScheduling
@EnableAsync
public class LinkedApplication {

    public static void main(String[] args) {
        SpringApplication.run(LinkedApplication.class, args);
    }

    /** Hora actual inyectable: los tests la fijan para probar vencimientos. */
    @Bean
    public Clock reloj() {
        return Clock.systemDefaultZone();
    }
}
