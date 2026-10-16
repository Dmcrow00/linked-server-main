package com.linked.integration;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Lo que linked usa de la API de Instagram (Instagram API con inicio de sesión de Instagram).
 * <p>
 * Es un puerto: {@link InstagramGraphApi} habla con Meta y los tests usan una implementación
 * falsa, así ningún test sale a la red.
 */
public interface InstagramApi {

    /** URL de Instagram a la que se redirige al tatuador para que autorice la vinculación. */
    String urlDeAutorizacion(String state);

    /** Canjea el código del callback por un token de larga duración (unos 60 días). */
    TokenDeAcceso canjearCodigo(String code);

    /** Extiende la vigencia de un token de larga duración que todavía no venció. */
    TokenDeAcceso renovar(String accessToken);

    Cuenta cuenta(String accessToken);

    /** Todas las publicaciones de la cuenta (recorre todas las páginas), con las imágenes de cada una. */
    List<Publicacion> publicaciones(String accessToken);

    /** Descarga una imagen: las URLs de Instagram vencen, por eso se copian a Cloudinary. */
    byte[] descargar(String url);

    /**
     * @param instagramUserId  id de la cuenta de Instagram; puede venir vacío al renovar
     * @param segundosDeVida   vigencia informada por Instagram
     */
    record TokenDeAcceso(String accessToken, long segundosDeVida, String instagramUserId) {

        @Override
        public String toString() {
            return "TokenDeAcceso[accessToken=[oculto], segundosDeVida=" + segundosDeVida + "]";
        }
    }

    /** @param fotoDePerfil URL de la foto; {@code null} si la cuenta no tiene */
    record Cuenta(String id, String username, String fotoDePerfil) {
    }

    /**
     * @param imagenes  URLs de las imágenes: una para {@code IMAGE}, las de cada elemento para
     *                  {@code CAROUSEL_ALBUM}, ninguna para {@code VIDEO}
     */
    record Publicacion(String id, String tipo, String descripcion, List<String> imagenes, LocalDateTime fecha) {

        public boolean esVideo() {
            return "VIDEO".equals(tipo);
        }
    }

    /** El token venció o fue revocado (error OAuth 190 de Meta): hay que volver a vincular la cuenta. */
    class TokenVencidoException extends RuntimeException {

        public TokenVencidoException(String mensaje) {
            super(mensaje);
        }
    }
}
