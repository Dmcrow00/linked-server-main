-- ============================================================
-- V2__create_linked_tables.sql
-- Tablas especificas de LINKED: dominio tat_* y hd_*
-- ============================================================

-- ────────────────────────────────────────────────────────────
-- 1. tat_especialidad  (estilos/especialidades de tatuaje)
-- ────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS tat_especialidad (
    id                      BIGSERIAL       NOT NULL,
    nombre                  VARCHAR(100)    NOT NULL,
    descripcion             TEXT,
    creationuser            VARCHAR(25)     NOT NULL DEFAULT 'SYSTEM',
    creationtimestamp       TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modificationuser        VARCHAR(25),
    modificationtimestamp   TIMESTAMP,
    status                  VARCHAR         NOT NULL DEFAULT 'true',
    version                 BIGINT          NOT NULL DEFAULT 0,
    CONSTRAINT tat_especialidad_pkey    PRIMARY KEY (id),
    CONSTRAINT tat_especialidad_nombre_key UNIQUE (nombre)
);

-- ────────────────────────────────────────────────────────────
-- 2. tat_tatuador_especialidad  (M:N tatuador ↔ especialidad)
-- ────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS tat_tatuador_especialidad (
    id_tatuador     BIGINT NOT NULL,
    id_especialidad BIGINT NOT NULL,
    CONSTRAINT tat_tatuador_esp_pkey PRIMARY KEY (id_tatuador, id_especialidad),
    CONSTRAINT tat_tatuador_esp_tatuador_fkey
        FOREIGN KEY (id_tatuador) REFERENCES swe_usrapl(id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT tat_tatuador_esp_especialidad_fkey
        FOREIGN KEY (id_especialidad) REFERENCES tat_especialidad(id)
        ON DELETE CASCADE ON UPDATE CASCADE
);
CREATE INDEX IF NOT EXISTS idx_tatuador_esp_tatuador    ON tat_tatuador_especialidad(id_tatuador);
CREATE INDEX IF NOT EXISTS idx_tatuador_esp_especialidad ON tat_tatuador_especialidad(id_especialidad);

-- ────────────────────────────────────────────────────────────
-- 3. tat_trabajo  (portfolio de trabajos del tatuador)
-- ────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS tat_trabajo (
    id                      BIGSERIAL       NOT NULL,
    descripcion             TEXT,
    id_tatuador             BIGINT          NOT NULL,
    instagram_media_id      VARCHAR(100),
    creationuser            VARCHAR(25)     NOT NULL DEFAULT 'SYSTEM',
    creationtimestamp       TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modificationuser        VARCHAR(25),
    modificationtimestamp   TIMESTAMP,
    status                  VARCHAR         NOT NULL DEFAULT 'true',
    version                 BIGINT          NOT NULL DEFAULT 0,
    CONSTRAINT tat_trabajo_pkey PRIMARY KEY (id),
    CONSTRAINT tat_trabajo_tatuador_fkey
        FOREIGN KEY (id_tatuador) REFERENCES swe_usrapl(id)
        ON DELETE CASCADE ON UPDATE CASCADE
);
CREATE INDEX IF NOT EXISTS idx_trabajo_tatuador         ON tat_trabajo(id_tatuador);
CREATE INDEX IF NOT EXISTS idx_trabajo_instagram_media  ON tat_trabajo(instagram_media_id);

-- ────────────────────────────────────────────────────────────
-- 4. tat_trabajo_foto  (fotos de un trabajo, max 5)
-- ────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS tat_trabajo_foto (
    id          BIGSERIAL   NOT NULL,
    image_path  TEXT        NOT NULL,
    id_trabajo  BIGINT      NOT NULL,
    CONSTRAINT tat_trabajo_foto_pkey PRIMARY KEY (id),
    CONSTRAINT tat_trabajo_foto_trabajo_fkey
        FOREIGN KEY (id_trabajo) REFERENCES tat_trabajo(id)
        ON DELETE CASCADE ON UPDATE CASCADE
);
CREATE INDEX IF NOT EXISTS idx_trabajo_foto_trabajo ON tat_trabajo_foto(id_trabajo);

-- ────────────────────────────────────────────────────────────
-- 5. tat_trabajo_favorito  (likes de clientes a trabajos)
-- ────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS tat_trabajo_favorito (
    id                  BIGSERIAL   NOT NULL,
    id_cliente          BIGINT      NOT NULL,
    id_trabajo          BIGINT      NOT NULL,
    creationtimestamp   TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version             BIGINT      NOT NULL DEFAULT 0,
    CONSTRAINT tat_trabajo_favorito_pkey    PRIMARY KEY (id),
    CONSTRAINT tat_trabajo_favorito_unique  UNIQUE (id_cliente, id_trabajo),
    CONSTRAINT tat_trabajo_favorito_cliente_fkey
        FOREIGN KEY (id_cliente) REFERENCES swe_usrapl(id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT tat_trabajo_favorito_trabajo_fkey
        FOREIGN KEY (id_trabajo) REFERENCES tat_trabajo(id)
        ON DELETE CASCADE ON UPDATE CASCADE
);
CREATE INDEX IF NOT EXISTS idx_favorito_cliente ON tat_trabajo_favorito(id_cliente);
CREATE INDEX IF NOT EXISTS idx_favorito_trabajo ON tat_trabajo_favorito(id_trabajo);

-- ────────────────────────────────────────────────────────────
-- 6. tat_turno_sesion  (solicitudes/reservas de turno)
-- ────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS tat_turno_sesion (
    id                      BIGSERIAL       NOT NULL,
    id_cliente              BIGINT,
    estado                  VARCHAR(20)     NOT NULL DEFAULT 'Pendiente',
    fecha_hora_inicio       TIMESTAMP       NOT NULL,
    fecha_hora_fin          TIMESTAMP       NOT NULL,
    descripcion_cliente     VARCHAR(1000),
    creationuser            VARCHAR(25)     NOT NULL DEFAULT 'SYSTEM',
    creationtimestamp       TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modificationuser        VARCHAR(25),
    modificationtimestamp   TIMESTAMP,
    version                 BIGINT          NOT NULL DEFAULT 0,
    CONSTRAINT tat_turno_sesion_pkey PRIMARY KEY (id),
    CONSTRAINT tat_turno_sesion_cliente_fkey
        FOREIGN KEY (id_cliente) REFERENCES swe_usrapl(id)
        ON DELETE SET NULL ON UPDATE CASCADE
);
CREATE INDEX IF NOT EXISTS idx_turno_sesion_cliente ON tat_turno_sesion(id_cliente);
CREATE INDEX IF NOT EXISTS idx_turno_sesion_estado  ON tat_turno_sesion(estado);
CREATE INDEX IF NOT EXISTS idx_turno_sesion_fecha   ON tat_turno_sesion(fecha_hora_inicio);

-- ────────────────────────────────────────────────────────────
-- 7. tat_turno_tatuador  (asignacion tatuador ↔ turno, PK compuesta)
-- ────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS tat_turno_tatuador (
    id_tatuador     BIGINT NOT NULL,
    id_turno_sesion BIGINT NOT NULL,
    CONSTRAINT tat_turno_tatuador_pkey PRIMARY KEY (id_tatuador, id_turno_sesion),
    CONSTRAINT tat_turno_tatuador_tatuador_fkey
        FOREIGN KEY (id_tatuador) REFERENCES swe_usrapl(id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT tat_turno_tatuador_turno_fkey
        FOREIGN KEY (id_turno_sesion) REFERENCES tat_turno_sesion(id)
        ON DELETE CASCADE ON UPDATE CASCADE
);
CREATE INDEX IF NOT EXISTS idx_turno_tatuador_tatuador ON tat_turno_tatuador(id_tatuador);
CREATE INDEX IF NOT EXISTS idx_turno_tatuador_turno    ON tat_turno_tatuador(id_turno_sesion);

-- ────────────────────────────────────────────────────────────
-- 8. tat_turno_mensajes  (chat entre cliente y tatuador)
-- ────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS tat_turno_mensajes (
    id                  BIGSERIAL   NOT NULL,
    mensaje             TEXT        NOT NULL,
    timestamp           TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    leido               BOOLEAN     NOT NULL DEFAULT FALSE,
    id_turno_sesion     BIGINT      NOT NULL,
    id_usuario_envia    BIGINT      NOT NULL,
    CONSTRAINT tat_turno_mensajes_pkey PRIMARY KEY (id),
    CONSTRAINT tat_turno_mensajes_turno_fkey
        FOREIGN KEY (id_turno_sesion) REFERENCES tat_turno_sesion(id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT tat_turno_mensajes_usuario_fkey
        FOREIGN KEY (id_usuario_envia) REFERENCES swe_usrapl(id)
        ON DELETE SET NULL ON UPDATE CASCADE
);
CREATE INDEX IF NOT EXISTS idx_turno_mensajes_turno   ON tat_turno_mensajes(id_turno_sesion);
CREATE INDEX IF NOT EXISTS idx_turno_mensajes_usuario ON tat_turno_mensajes(id_usuario_envia);

-- ────────────────────────────────────────────────────────────
-- 9. tat_imagen_ref  (imagenes de referencia del cliente en el turno)
-- ────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS tat_imagen_ref (
    id                      BIGSERIAL   NOT NULL,
    image_path              TEXT        NOT NULL,
    id_turno_sesion         BIGINT      NOT NULL,
    creationuser            VARCHAR(25) NOT NULL DEFAULT 'SYSTEM',
    creationtimestamp       TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modificationuser        VARCHAR(25),
    modificationtimestamp   TIMESTAMP,
    version                 BIGINT      NOT NULL DEFAULT 0,
    CONSTRAINT tat_imagen_ref_pkey PRIMARY KEY (id),
    CONSTRAINT tat_imagen_ref_turno_fkey
        FOREIGN KEY (id_turno_sesion) REFERENCES tat_turno_sesion(id)
        ON DELETE CASCADE ON UPDATE CASCADE
);
CREATE INDEX IF NOT EXISTS idx_imagen_ref_turno ON tat_imagen_ref(id_turno_sesion);

-- ────────────────────────────────────────────────────────────
-- 10. tat_horario_habitual  (horario semanal recurrente del tatuador)
-- ────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS tat_horario_habitual (
    id                  BIGSERIAL   NOT NULL,
    id_tatuador         BIGINT      NOT NULL,
    dia_semana          SMALLINT    NOT NULL CHECK (dia_semana BETWEEN 0 AND 6),
    hora_inicio         TIME        NOT NULL,
    hora_fin            TIME        NOT NULL,
    duracion_turno_min  INT         NOT NULL,
    creationuser        VARCHAR(25) NOT NULL DEFAULT 'SYSTEM',
    creationtimestamp   TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT tat_horario_habitual_pkey    PRIMARY KEY (id),
    CONSTRAINT tat_horario_habitual_unique  UNIQUE (id_tatuador, dia_semana),
    CONSTRAINT tat_horario_habitual_tatuador_fkey
        FOREIGN KEY (id_tatuador) REFERENCES swe_usrapl(id)
        ON DELETE CASCADE ON UPDATE CASCADE
);
CREATE INDEX IF NOT EXISTS idx_horario_habitual_tatuador ON tat_horario_habitual(id_tatuador);

-- ────────────────────────────────────────────────────────────
-- 11. tat_instagram_token  (tokens OAuth de Instagram por tatuador)
-- ────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS tat_instagram_token (
    id                      BIGSERIAL   NOT NULL,
    id_tatuador             BIGINT      NOT NULL,
    access_token            TEXT        NOT NULL,
    instagram_user_id       VARCHAR(100) NOT NULL,
    token_expires_at        TIMESTAMP   NOT NULL,
    creationtimestamp       TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modificationtimestamp   TIMESTAMP,
    CONSTRAINT tat_instagram_token_pkey             PRIMARY KEY (id),
    CONSTRAINT tat_instagram_token_tatuador_unique  UNIQUE (id_tatuador),
    CONSTRAINT tat_instagram_token_tatuador_fkey
        FOREIGN KEY (id_tatuador) REFERENCES swe_usrapl(id)
        ON DELETE CASCADE ON UPDATE CASCADE
);

-- ────────────────────────────────────────────────────────────
-- 12. tat_solicitud_tatuador  (solicitudes para ser tatuador)
-- ────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS tat_solicitud_tatuador (
    id                      BIGSERIAL       NOT NULL,
    id_user                 BIGINT          NOT NULL,
    estudio                 VARCHAR(100),
    especialidades_ids      JSONB,
    status                  VARCHAR(20)     NOT NULL DEFAULT 'pendiente',
    notas                   TEXT,
    admin_user              VARCHAR(100),
    creationtimestamp       TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modificationtimestamp   TIMESTAMP,
    CONSTRAINT tat_solicitud_tatuador_pkey      PRIMARY KEY (id),
    CONSTRAINT tat_solicitud_tatuador_user_fkey
        FOREIGN KEY (id_user) REFERENCES swe_usrapl(id)
        ON DELETE CASCADE ON UPDATE CASCADE
);
CREATE INDEX IF NOT EXISTS idx_solicitud_tatuador_user   ON tat_solicitud_tatuador(id_user);
CREATE INDEX IF NOT EXISTS idx_solicitud_tatuador_status ON tat_solicitud_tatuador(status);

-- ────────────────────────────────────────────────────────────
-- 13. hd_support_ticket  (tickets de soporte)
-- ────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS hd_support_ticket (
    id                      BIGSERIAL       NOT NULL,
    status                  BOOLEAN         NOT NULL DEFAULT TRUE,
    description             TEXT            NOT NULL,
    category                VARCHAR(50),
    priority                VARCHAR(20),
    url_pagina              VARCHAR(500),
    user_agent              VARCHAR(500),
    screenshot              TEXT,
    admin_response          TEXT,
    contact_email           VARCHAR(254),
    creationuser            VARCHAR(25)     NOT NULL DEFAULT 'SYSTEM',
    creationtimestamp       TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modificationuser        VARCHAR(25),
    modificationtimestamp   TIMESTAMP,
    version                 BIGINT          NOT NULL DEFAULT 0,
    CONSTRAINT hd_support_ticket_pkey PRIMARY KEY (id)
);
CREATE INDEX IF NOT EXISTS idx_support_ticket_status ON hd_support_ticket(status);

-- ────────────────────────────────────────────────────────────
-- 14. hd_usr_st  (M:N soporte ticket ↔ usuario)
-- ────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS hd_usr_st (
    id_ticket   BIGINT NOT NULL,
    id_user     BIGINT NOT NULL,
    CONSTRAINT hd_usr_st_pkey PRIMARY KEY (id_ticket, id_user),
    CONSTRAINT hd_usr_st_ticket_fkey
        FOREIGN KEY (id_ticket) REFERENCES hd_support_ticket(id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT hd_usr_st_user_fkey
        FOREIGN KEY (id_user) REFERENCES swe_usrapl(id)
        ON DELETE CASCADE ON UPDATE CASCADE
);
