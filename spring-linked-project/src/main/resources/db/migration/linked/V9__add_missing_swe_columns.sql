-- ================================================================
-- V9: Agregar columnas de dominio faltantes en tablas swe_*
--     creadas con una versión anterior de V1
-- ================================================================

-- ────────────────────────────────────────────────────────────────
-- swe_itemmenu: columnas de dominio agregadas después del DDL original
-- ────────────────────────────────────────────────────────────────
ALTER TABLE swe_itemmenu
    ADD COLUMN IF NOT EXISTS icon            VARCHAR(100),
    ADD COLUMN IF NOT EXISTS route           VARCHAR(255),
    ADD COLUMN IF NOT EXISTS order_number    INT,
    ADD COLUMN IF NOT EXISTS roles_permitidos VARCHAR(500);

-- ────────────────────────────────────────────────────────────────
-- swe_usrapl: columnas de perfil agregadas después del DDL original
-- birth_date: NOT NULL en entidad → DEFAULT provisional para filas existentes
-- ────────────────────────────────────────────────────────────────
ALTER TABLE swe_usrapl
    ADD COLUMN IF NOT EXISTS birth_date              DATE           NOT NULL DEFAULT '1990-01-01',
    ADD COLUMN IF NOT EXISTS profile_photo           TEXT,
    ADD COLUMN IF NOT EXISTS estudio                 VARCHAR(100),
    ADD COLUMN IF NOT EXISTS fecha_inicio_actividad  DATE,
    ADD COLUMN IF NOT EXISTS delete_date             TIMESTAMP,
    ADD COLUMN IF NOT EXISTS reset_password_token    TEXT,
    ADD COLUMN IF NOT EXISTS reset_password_expires  TIMESTAMP,
    ADD COLUMN IF NOT EXISTS localidad               VARCHAR(255),
    ADD COLUMN IF NOT EXISTS lat                     DOUBLE PRECISION,
    ADD COLUMN IF NOT EXISTS lng                     DOUBLE PRECISION,
    ADD COLUMN IF NOT EXISTS instagram_handle        VARCHAR(100);
