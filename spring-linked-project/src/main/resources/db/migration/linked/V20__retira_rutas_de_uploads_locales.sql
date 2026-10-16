-- ================================================================
-- V20: Las imágenes guardadas en el disco del backend Node dejan de referenciarse
--
-- Node guardaba algunas imágenes en su carpeta uploads/ y en la base dejaba la ruta
-- relativa (/uploads/users/..., /uploads/turnos/...). Spring guarda todo en Cloudinary y no
-- sirve archivos locales (docs/decisiones/ADR-0001). Esas filas apuntan a algo que ya no
-- existe para la API: se sacan de las tablas y la ruta queda registrada en
-- tat_ruta_local_retirada, por si alguna imagen se sube a mano a Cloudinary y hay que
-- volver a enlazarla. Los archivos están respaldados fuera del repositorio.
--
-- En una base creada por Spring no hay ninguna fila así y esto no cambia nada.
-- ================================================================

CREATE TABLE tat_ruta_local_retirada (
    id          BIGSERIAL    NOT NULL,
    tabla       VARCHAR(50)  NOT NULL,
    id_fila     BIGINT       NOT NULL,
    id_padre    BIGINT,
    ruta        TEXT         NOT NULL,
    retirada_el TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT tat_ruta_local_retirada_pkey PRIMARY KEY (id)
);

-- Foto de perfil: la columna es opcional, el cliente muestra el avatar por defecto.
INSERT INTO tat_ruta_local_retirada (tabla, id_fila, ruta)
SELECT 'swe_usrapl', id, profile_photo FROM swe_usrapl WHERE profile_photo LIKE '/uploads/%';
UPDATE swe_usrapl SET profile_photo = NULL WHERE profile_photo LIKE '/uploads/%';

-- Fotos de trabajos (id_padre = trabajo) e imágenes de referencia de turnos (id_padre = turno).
INSERT INTO tat_ruta_local_retirada (tabla, id_fila, id_padre, ruta)
SELECT 'tat_trabajo_foto', id, id_trabajo, image_path FROM tat_trabajo_foto WHERE image_path LIKE '/uploads/%';
DELETE FROM tat_trabajo_foto WHERE image_path LIKE '/uploads/%';

INSERT INTO tat_ruta_local_retirada (tabla, id_fila, id_padre, ruta)
SELECT 'tat_imagen_ref', id, id_turno_sesion, image_path FROM tat_imagen_ref WHERE image_path LIKE '/uploads/%';
DELETE FROM tat_imagen_ref WHERE image_path LIKE '/uploads/%';
