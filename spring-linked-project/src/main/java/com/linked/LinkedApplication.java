package com.linked;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Punto de entrada de la API de LINKED.
 * <p>
 * {@code @EnableScheduling} habilita la renovación diaria de tokens de Instagram y
 * {@code @EnableAsync} el envío de emails y las sincronizaciones en segundo plano.
 */
@SpringBootApplication
@EnableScheduling
@EnableAsync
public class LinkedApplication {

    public static void main(String[] args) {
        SpringApplication.run(LinkedApplication.class, args);
    }
}
