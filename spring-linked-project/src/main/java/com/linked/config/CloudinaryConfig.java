package com.linked.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cliente de Cloudinary, donde se almacenan las imágenes que se suben a la plataforma
 * (fotos de trabajos, referencias de turnos y fotos de perfil).
 */
@Configuration
public class CloudinaryConfig {

    @Bean
    public Cloudinary cloudinary(@Value("${app.cloudinary.cloud-name}") String cloudName,
                                 @Value("${app.cloudinary.api-key}") String apiKey,
                                 @Value("${app.cloudinary.api-secret}") String apiSecret) {
        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret,
                "secure", true));
    }
}
