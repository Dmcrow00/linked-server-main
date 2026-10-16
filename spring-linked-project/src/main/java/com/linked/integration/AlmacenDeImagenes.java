package com.linked.integration;

import org.springframework.web.multipart.MultipartFile;

/**
 * Dónde se guardan las imágenes que se suben a la plataforma (fotos de trabajos, referencias de
 * turnos, fotos de perfil). Devuelve la URL pública, que es lo que se persiste.
 * <p>
 * Es un puerto: {@link CloudinaryAlmacenDeImagenes} sube a Cloudinary y los tests usan uno falso.
 */
public interface AlmacenDeImagenes {

    /**
     * Valida tipo y tamaño antes de subir.
     *
     * @param carpeta subcarpeta dentro de {@code linked/} (p. ej. {@code trabajos})
     * @return URL https de la imagen
     */
    String subir(MultipartFile archivo, String carpeta);

    /** Para imágenes ya descargadas de otro servicio (Instagram); no se validan. */
    String subir(byte[] imagen, String carpeta);
}
