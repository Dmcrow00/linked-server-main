-- ================================================================
-- V19: Datos propios de linked fuera de swe_usrapl
--
-- core-swe mapea solo las columnas comunes de swe_usrapl (ADR-0004 de dmc00-core). Lo que
-- es de linked (estudio, inicio de actividad, ubicación e Instagram) pasa a una tabla de
-- perfil con el mismo id del usuario. Se crea un perfil para cada usuario existente, tenga
-- o no datos, así toda cuenta tiene el suyo.
--
-- Las columnas viejas de swe_usrapl se conservan: la versión anterior de la app las sigue
-- leyendo mientras convivan (despliegue sin corte). Las borra una migración de la versión
-- siguiente.
-- ================================================================

CREATE TABLE tat_perfil_usuario (
    id_usrapl               BIGINT           NOT NULL,
    estudio                 VARCHAR(100),
    fecha_inicio_actividad  DATE,
    localidad               VARCHAR(255),
    lat                     DOUBLE PRECISION,
    lng                     DOUBLE PRECISION,
    instagram_handle        VARCHAR(100),
    CONSTRAINT tat_perfil_usuario_pkey PRIMARY KEY (id_usrapl),
    CONSTRAINT tat_perfil_usuario_id_usrapl_fkey
        FOREIGN KEY (id_usrapl) REFERENCES swe_usrapl(id)
        ON DELETE CASCADE ON UPDATE CASCADE
);

-- La búsqueda por mapa filtra por coordenadas.
CREATE INDEX idx_perfil_usuario_ubicacion ON tat_perfil_usuario(lat, lng);

INSERT INTO tat_perfil_usuario
    (id_usrapl, estudio, fecha_inicio_actividad, localidad, lat, lng, instagram_handle)
SELECT id, estudio, fecha_inicio_actividad, localidad, lat, lng, instagram_handle
FROM swe_usrapl;
