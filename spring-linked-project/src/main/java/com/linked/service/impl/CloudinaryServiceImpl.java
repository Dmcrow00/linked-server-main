package com.linked.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.linked.exception.AppExceptions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

/**
 * Subida y borrado de imágenes en Cloudinary.
 * <p>
 * Las imágenes se guardan bajo la carpeta {@code linked/<folder>} y se devuelve la URL segura
 * (https), que es lo que se persiste en base de datos.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CloudinaryServiceImpl {

    private final Cloudinary cloudinary;

    /** @return URL https de la imagen subida */
    @SuppressWarnings("unchecked")
    public String uploadImage(MultipartFile file, String folder) {
        try {
            Map<String, Object> result = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "linked/" + folder,
                            "resource_type", "image"
                    )
            );
            return (String) result.get("secure_url");
        } catch (IOException e) {
            log.error("Error uploading image to Cloudinary: {}", e.getMessage());
            throw new AppExceptions.InternalException("Error al subir la imagen");
        }
    }

    /** Igual que {@link #uploadImage} pero a partir de bytes ya descargados (por ejemplo, desde Instagram). */
    @SuppressWarnings("unchecked")
    public String uploadBytes(byte[] bytes, String folder) {
        try {
            Map<String, Object> result = cloudinary.uploader().upload(
                    bytes,
                    ObjectUtils.asMap(
                            "folder", "linked/" + folder,
                            "resource_type", "image"
                    )
            );
            return (String) result.get("secure_url");
        } catch (IOException e) {
            log.error("Error uploading bytes to Cloudinary: {}", e.getMessage());
            throw new AppExceptions.InternalException("Error al subir la imagen");
        }
    }

    /** Borra una imagen por su {@code public_id}; si falla solo se registra en el log. */
    public void deleteImage(String publicId) {
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (IOException e) {
            log.warn("Error deleting image from Cloudinary: {}", e.getMessage());
        }
    }
}
