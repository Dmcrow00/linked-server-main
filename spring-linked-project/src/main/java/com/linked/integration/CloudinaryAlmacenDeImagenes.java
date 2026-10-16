package com.linked.integration;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.dmc00.core.web.AppExceptions;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

/** {@link AlmacenDeImagenes} en Cloudinary, bajo la carpeta {@code linked/<carpeta>}. */
@Component
@RequiredArgsConstructor
public class CloudinaryAlmacenDeImagenes implements AlmacenDeImagenes {

    static final long MAXIMO_DE_BYTES = 5L * 1024 * 1024;
    static final Set<String> TIPOS_PERMITIDOS = Set.of("image/jpeg", "image/png", "image/webp");

    private final Cloudinary cloudinary;

    @Override
    public String subir(MultipartFile archivo, String carpeta) {
        validar(archivo);
        try {
            return subir(archivo.getBytes(), carpeta);
        } catch (IOException ex) {
            throw new AppExceptions.BadRequestException("No se pudo leer la imagen recibida");
        }
    }

    @Override
    public String subir(byte[] imagen, String carpeta) {
        try {
            Map<?, ?> resultado = cloudinary.uploader().upload(imagen,
                    ObjectUtils.asMap("folder", "linked/" + carpeta, "resource_type", "image"));
            return (String) resultado.get("secure_url");
        } catch (IOException | RuntimeException ex) {
            throw new AppExceptions.ServicioExternoException("No se pudo subir la imagen", ex);
        }
    }

    /**
     * Rechaza lo que no sea una imagen soportada antes de gastar la subida. El tipo lo declara el
     * cliente, así que no reemplaza a la validación de Cloudinary: evita que cualquier archivo
     * termine publicado bajo una URL del estudio.
     */
    static void validar(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new AppExceptions.BadRequestException("El archivo de imagen está vacío");
        }
        if (archivo.getSize() > MAXIMO_DE_BYTES) {
            throw new AppExceptions.BadRequestException("La imagen no puede superar los 5 MB");
        }
        String tipo = archivo.getContentType();
        if (tipo == null || !TIPOS_PERMITIDOS.contains(tipo.toLowerCase())) {
            throw new AppExceptions.BadRequestException("Formato de imagen no soportado: se aceptan JPG, PNG o WEBP");
        }
    }
}
