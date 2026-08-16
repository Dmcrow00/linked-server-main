-- ================================================================
-- V3: Migrar entidades de dominio a BaseEntity (dmc00-core)
-- Agregar columnas faltantes en swe_usrauth y swe_usrrolapl
-- ================================================================

-- ────────────────────────────────────────────────────────────────
-- SWE: swe_usrauth → SweAuditableEntity
-- Faltan: modificationuser, status
-- ────────────────────────────────────────────────────────────────
ALTER TABLE swe_usrauth
    ADD COLUMN IF NOT EXISTS modificationuser VARCHAR(25),
    ADD COLUMN IF NOT EXISTS status           VARCHAR NOT NULL DEFAULT 'true';

-- ────────────────────────────────────────────────────────────────
-- SWE: swe_usrrolapl → SweAuditableEntity
-- Faltan: modificationuser, modificationtimestamp
-- ────────────────────────────────────────────────────────────────
ALTER TABLE swe_usrrolapl
    ADD COLUMN IF NOT EXISTS modificationuser      VARCHAR(25),
    ADD COLUMN IF NOT EXISTS modificationtimestamp TIMESTAMP;

-- ────────────────────────────────────────────────────────────────
-- tat_especialidad: SweAuditableEntity → BaseEntity
-- ────────────────────────────────────────────────────────────────
ALTER TABLE tat_especialidad
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS active     BOOLEAN   NOT NULL DEFAULT TRUE;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='tat_especialidad' AND column_name='creationtimestamp') THEN
        UPDATE tat_especialidad
           SET created_at = creationtimestamp,
               updated_at = modificationtimestamp,
               active     = (status = 'true');
    END IF;
END $$;

ALTER TABLE tat_especialidad
    DROP COLUMN IF EXISTS creationuser,
    DROP COLUMN IF EXISTS creationtimestamp,
    DROP COLUMN IF EXISTS modificationuser,
    DROP COLUMN IF EXISTS modificationtimestamp,
    DROP COLUMN IF EXISTS status;

-- ────────────────────────────────────────────────────────────────
-- tat_trabajo: SweAuditableEntity → BaseEntity
-- ────────────────────────────────────────────────────────────────
ALTER TABLE tat_trabajo
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS active     BOOLEAN   NOT NULL DEFAULT TRUE;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='tat_trabajo' AND column_name='creationtimestamp') THEN
        UPDATE tat_trabajo
           SET created_at = creationtimestamp,
               updated_at = modificationtimestamp,
               active     = (status = 'true');
    END IF;
END $$;

ALTER TABLE tat_trabajo
    DROP COLUMN IF EXISTS creationuser,
    DROP COLUMN IF EXISTS creationtimestamp,
    DROP COLUMN IF EXISTS modificationuser,
    DROP COLUMN IF EXISTS modificationtimestamp,
    DROP COLUMN IF EXISTS status;

-- ────────────────────────────────────────────────────────────────
-- tat_turno_sesion: SweBaseEntity → BaseEntity
-- ────────────────────────────────────────────────────────────────
ALTER TABLE tat_turno_sesion
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS active     BOOLEAN   NOT NULL DEFAULT TRUE;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='tat_turno_sesion' AND column_name='creationtimestamp') THEN
        UPDATE tat_turno_sesion
           SET created_at = creationtimestamp,
               updated_at = modificationtimestamp;
    END IF;
END $$;

ALTER TABLE tat_turno_sesion
    DROP COLUMN IF EXISTS creationuser,
    DROP COLUMN IF EXISTS creationtimestamp,
    DROP COLUMN IF EXISTS modificationuser,
    DROP COLUMN IF EXISTS modificationtimestamp;

-- ────────────────────────────────────────────────────────────────
-- hd_support_ticket: SweBaseEntity → BaseEntity
-- status BOOLEAN permanece (campo de dominio, no soft-delete)
-- ────────────────────────────────────────────────────────────────
ALTER TABLE hd_support_ticket
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS active     BOOLEAN   NOT NULL DEFAULT TRUE;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='hd_support_ticket' AND column_name='creationtimestamp') THEN
        UPDATE hd_support_ticket
           SET created_at = creationtimestamp,
               updated_at = modificationtimestamp;
    END IF;
END $$;

ALTER TABLE hd_support_ticket
    DROP COLUMN IF EXISTS creationuser,
    DROP COLUMN IF EXISTS creationtimestamp,
    DROP COLUMN IF EXISTS modificationuser,
    DROP COLUMN IF EXISTS modificationtimestamp;

-- ────────────────────────────────────────────────────────────────
-- tat_imagen_ref: SweBaseEntity → BaseEntity
-- ────────────────────────────────────────────────────────────────
ALTER TABLE tat_imagen_ref
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS active     BOOLEAN   NOT NULL DEFAULT TRUE;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='tat_imagen_ref' AND column_name='creationtimestamp') THEN
        UPDATE tat_imagen_ref
           SET created_at = creationtimestamp,
               updated_at = modificationtimestamp;
    END IF;
END $$;

ALTER TABLE tat_imagen_ref
    DROP COLUMN IF EXISTS creationuser,
    DROP COLUMN IF EXISTS creationtimestamp,
    DROP COLUMN IF EXISTS modificationuser,
    DROP COLUMN IF EXISTS modificationtimestamp;

-- ────────────────────────────────────────────────────────────────
-- tat_trabajo_foto: sin herencia → BaseEntity
-- ────────────────────────────────────────────────────────────────
ALTER TABLE tat_trabajo_foto
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS active     BOOLEAN   NOT NULL DEFAULT TRUE,
    ADD COLUMN IF NOT EXISTS version    BIGINT    NOT NULL DEFAULT 0;

-- ────────────────────────────────────────────────────────────────
-- tat_turno_mensajes: sin herencia → BaseEntity
-- ────────────────────────────────────────────────────────────────
ALTER TABLE tat_turno_mensajes
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS active     BOOLEAN   NOT NULL DEFAULT TRUE,
    ADD COLUMN IF NOT EXISTS version    BIGINT    NOT NULL DEFAULT 0;

-- ────────────────────────────────────────────────────────────────
-- tat_trabajo_favorito: auditoría custom → BaseEntity
-- ────────────────────────────────────────────────────────────────
ALTER TABLE tat_trabajo_favorito
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS active     BOOLEAN   NOT NULL DEFAULT TRUE;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='tat_trabajo_favorito' AND column_name='creationtimestamp') THEN
        UPDATE tat_trabajo_favorito SET created_at = creationtimestamp;
    END IF;
END $$;

ALTER TABLE tat_trabajo_favorito DROP COLUMN IF EXISTS creationtimestamp;

-- ────────────────────────────────────────────────────────────────
-- tat_solicitud_tatuador: auditoría custom → BaseEntity
-- status VARCHAR(20) permanece (campo de dominio SolicitudStatus)
-- ────────────────────────────────────────────────────────────────
ALTER TABLE tat_solicitud_tatuador
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS active     BOOLEAN   NOT NULL DEFAULT TRUE,
    ADD COLUMN IF NOT EXISTS version    BIGINT    NOT NULL DEFAULT 0;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='tat_solicitud_tatuador' AND column_name='creationtimestamp') THEN
        UPDATE tat_solicitud_tatuador
           SET created_at = creationtimestamp,
               updated_at = modificationtimestamp;
    END IF;
END $$;

ALTER TABLE tat_solicitud_tatuador
    DROP COLUMN IF EXISTS creationtimestamp,
    DROP COLUMN IF EXISTS modificationtimestamp;

-- ────────────────────────────────────────────────────────────────
-- tat_instagram_token: auditoría custom → BaseEntity
-- ────────────────────────────────────────────────────────────────
ALTER TABLE tat_instagram_token
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS active     BOOLEAN   NOT NULL DEFAULT TRUE,
    ADD COLUMN IF NOT EXISTS version    BIGINT    NOT NULL DEFAULT 0;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='tat_instagram_token' AND column_name='creationtimestamp') THEN
        UPDATE tat_instagram_token
           SET created_at = creationtimestamp,
               updated_at = modificationtimestamp;
    END IF;
END $$;

ALTER TABLE tat_instagram_token
    DROP COLUMN IF EXISTS creationtimestamp,
    DROP COLUMN IF EXISTS modificationtimestamp;

-- ────────────────────────────────────────────────────────────────
-- tat_horario_habitual: auditoría custom → BaseEntity
-- ────────────────────────────────────────────────────────────────
ALTER TABLE tat_horario_habitual
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS active     BOOLEAN   NOT NULL DEFAULT TRUE,
    ADD COLUMN IF NOT EXISTS version    BIGINT    NOT NULL DEFAULT 0;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='tat_horario_habitual' AND column_name='creationtimestamp') THEN
        UPDATE tat_horario_habitual SET created_at = creationtimestamp;
    END IF;
END $$;

ALTER TABLE tat_horario_habitual
    DROP COLUMN IF EXISTS creationuser,
    DROP COLUMN IF EXISTS creationtimestamp;
