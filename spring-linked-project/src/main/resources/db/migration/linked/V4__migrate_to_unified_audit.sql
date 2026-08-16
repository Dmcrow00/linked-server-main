-- ================================================================
-- V4: Migrar todas las entidades al patrón de auditoría unificado
--     de BaseEntity: creationuser, creationtimestamp,
--     modificationuser, modificationtimestamp, status (VARCHAR)
-- ================================================================

-- ────────────────────────────────────────────────────────────────
-- Renombrar columnas de dominio que colisionan con BaseEntity.status
-- ────────────────────────────────────────────────────────────────

-- tat_solicitud_tatuador: status (SolicitudStatus enum) → solicitud_status
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns
               WHERE table_name='tat_solicitud_tatuador' AND column_name='status')
       AND NOT EXISTS (SELECT 1 FROM information_schema.columns
                       WHERE table_name='tat_solicitud_tatuador' AND column_name='solicitud_status') THEN
        ALTER TABLE tat_solicitud_tatuador RENAME COLUMN status TO solicitud_status;
    END IF;
END $$;

-- hd_support_ticket: status (BOOLEAN ticket abierto/cerrado) → ticket_open
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns
               WHERE table_name='hd_support_ticket' AND column_name='status')
       AND NOT EXISTS (SELECT 1 FROM information_schema.columns
                       WHERE table_name='hd_support_ticket' AND column_name='ticket_open') THEN
        ALTER TABLE hd_support_ticket RENAME COLUMN status TO ticket_open;
    END IF;
END $$;

-- ────────────────────────────────────────────────────────────────
-- tat_especialidad
-- ────────────────────────────────────────────────────────────────
ALTER TABLE tat_especialidad
    ADD COLUMN IF NOT EXISTS creationuser          VARCHAR(25) NOT NULL DEFAULT 'SYSTEM',
    ADD COLUMN IF NOT EXISTS creationtimestamp     TIMESTAMP   NOT NULL DEFAULT NOW(),
    ADD COLUMN IF NOT EXISTS modificationuser      VARCHAR(25),
    ADD COLUMN IF NOT EXISTS modificationtimestamp TIMESTAMP,
    ADD COLUMN IF NOT EXISTS status                VARCHAR     NOT NULL DEFAULT 'true';

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns
               WHERE table_name='tat_especialidad' AND column_name='created_at') THEN
        UPDATE tat_especialidad
           SET creationtimestamp     = created_at,
               modificationtimestamp = updated_at,
               status                = CASE WHEN active THEN 'true' ELSE 'false' END;
    END IF;
END $$;

ALTER TABLE tat_especialidad
    DROP COLUMN IF EXISTS created_at,
    DROP COLUMN IF EXISTS updated_at,
    DROP COLUMN IF EXISTS active;

-- ────────────────────────────────────────────────────────────────
-- tat_trabajo
-- ────────────────────────────────────────────────────────────────
ALTER TABLE tat_trabajo
    ADD COLUMN IF NOT EXISTS creationuser          VARCHAR(25) NOT NULL DEFAULT 'SYSTEM',
    ADD COLUMN IF NOT EXISTS creationtimestamp     TIMESTAMP   NOT NULL DEFAULT NOW(),
    ADD COLUMN IF NOT EXISTS modificationuser      VARCHAR(25),
    ADD COLUMN IF NOT EXISTS modificationtimestamp TIMESTAMP,
    ADD COLUMN IF NOT EXISTS status                VARCHAR     NOT NULL DEFAULT 'true';

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns
               WHERE table_name='tat_trabajo' AND column_name='created_at') THEN
        UPDATE tat_trabajo
           SET creationtimestamp     = created_at,
               modificationtimestamp = updated_at,
               status                = CASE WHEN active THEN 'true' ELSE 'false' END;
    END IF;
END $$;

ALTER TABLE tat_trabajo
    DROP COLUMN IF EXISTS created_at,
    DROP COLUMN IF EXISTS updated_at,
    DROP COLUMN IF EXISTS active;

-- ────────────────────────────────────────────────────────────────
-- tat_turno_sesion
-- ────────────────────────────────────────────────────────────────
ALTER TABLE tat_turno_sesion
    ADD COLUMN IF NOT EXISTS creationuser          VARCHAR(25) NOT NULL DEFAULT 'SYSTEM',
    ADD COLUMN IF NOT EXISTS creationtimestamp     TIMESTAMP   NOT NULL DEFAULT NOW(),
    ADD COLUMN IF NOT EXISTS modificationuser      VARCHAR(25),
    ADD COLUMN IF NOT EXISTS modificationtimestamp TIMESTAMP,
    ADD COLUMN IF NOT EXISTS status                VARCHAR     NOT NULL DEFAULT 'true';

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns
               WHERE table_name='tat_turno_sesion' AND column_name='created_at') THEN
        UPDATE tat_turno_sesion
           SET creationtimestamp     = created_at,
               modificationtimestamp = updated_at;
    END IF;
END $$;

ALTER TABLE tat_turno_sesion
    DROP COLUMN IF EXISTS created_at,
    DROP COLUMN IF EXISTS updated_at,
    DROP COLUMN IF EXISTS active;

-- ────────────────────────────────────────────────────────────────
-- hd_support_ticket  (ticket_open = dominio; status = auditoría)
-- ────────────────────────────────────────────────────────────────
ALTER TABLE hd_support_ticket
    ADD COLUMN IF NOT EXISTS creationuser          VARCHAR(25) NOT NULL DEFAULT 'SYSTEM',
    ADD COLUMN IF NOT EXISTS creationtimestamp     TIMESTAMP   NOT NULL DEFAULT NOW(),
    ADD COLUMN IF NOT EXISTS modificationuser      VARCHAR(25),
    ADD COLUMN IF NOT EXISTS modificationtimestamp TIMESTAMP,
    ADD COLUMN IF NOT EXISTS status                VARCHAR     NOT NULL DEFAULT 'true';

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns
               WHERE table_name='hd_support_ticket' AND column_name='created_at') THEN
        UPDATE hd_support_ticket
           SET creationtimestamp     = created_at,
               modificationtimestamp = updated_at;
    END IF;
END $$;

ALTER TABLE hd_support_ticket
    DROP COLUMN IF EXISTS created_at,
    DROP COLUMN IF EXISTS updated_at,
    DROP COLUMN IF EXISTS active;

-- ────────────────────────────────────────────────────────────────
-- tat_imagen_ref
-- ────────────────────────────────────────────────────────────────
ALTER TABLE tat_imagen_ref
    ADD COLUMN IF NOT EXISTS creationuser          VARCHAR(25) NOT NULL DEFAULT 'SYSTEM',
    ADD COLUMN IF NOT EXISTS creationtimestamp     TIMESTAMP   NOT NULL DEFAULT NOW(),
    ADD COLUMN IF NOT EXISTS modificationuser      VARCHAR(25),
    ADD COLUMN IF NOT EXISTS modificationtimestamp TIMESTAMP,
    ADD COLUMN IF NOT EXISTS status                VARCHAR     NOT NULL DEFAULT 'true';

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns
               WHERE table_name='tat_imagen_ref' AND column_name='created_at') THEN
        UPDATE tat_imagen_ref
           SET creationtimestamp     = created_at,
               modificationtimestamp = updated_at;
    END IF;
END $$;

ALTER TABLE tat_imagen_ref
    DROP COLUMN IF EXISTS created_at,
    DROP COLUMN IF EXISTS updated_at,
    DROP COLUMN IF EXISTS active;

-- ────────────────────────────────────────────────────────────────
-- tat_trabajo_foto
-- ────────────────────────────────────────────────────────────────
ALTER TABLE tat_trabajo_foto
    ADD COLUMN IF NOT EXISTS creationuser          VARCHAR(25) NOT NULL DEFAULT 'SYSTEM',
    ADD COLUMN IF NOT EXISTS creationtimestamp     TIMESTAMP   NOT NULL DEFAULT NOW(),
    ADD COLUMN IF NOT EXISTS modificationuser      VARCHAR(25),
    ADD COLUMN IF NOT EXISTS modificationtimestamp TIMESTAMP,
    ADD COLUMN IF NOT EXISTS status                VARCHAR     NOT NULL DEFAULT 'true';

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns
               WHERE table_name='tat_trabajo_foto' AND column_name='created_at') THEN
        UPDATE tat_trabajo_foto
           SET creationtimestamp     = created_at,
               modificationtimestamp = updated_at;
    END IF;
END $$;

ALTER TABLE tat_trabajo_foto
    DROP COLUMN IF EXISTS created_at,
    DROP COLUMN IF EXISTS updated_at,
    DROP COLUMN IF EXISTS active;

-- ────────────────────────────────────────────────────────────────
-- tat_turno_mensajes
-- ────────────────────────────────────────────────────────────────
ALTER TABLE tat_turno_mensajes
    ADD COLUMN IF NOT EXISTS creationuser          VARCHAR(25) NOT NULL DEFAULT 'SYSTEM',
    ADD COLUMN IF NOT EXISTS creationtimestamp     TIMESTAMP   NOT NULL DEFAULT NOW(),
    ADD COLUMN IF NOT EXISTS modificationuser      VARCHAR(25),
    ADD COLUMN IF NOT EXISTS modificationtimestamp TIMESTAMP,
    ADD COLUMN IF NOT EXISTS status                VARCHAR     NOT NULL DEFAULT 'true';

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns
               WHERE table_name='tat_turno_mensajes' AND column_name='created_at') THEN
        UPDATE tat_turno_mensajes
           SET creationtimestamp     = created_at,
               modificationtimestamp = updated_at;
    END IF;
END $$;

ALTER TABLE tat_turno_mensajes
    DROP COLUMN IF EXISTS created_at,
    DROP COLUMN IF EXISTS updated_at,
    DROP COLUMN IF EXISTS active;

-- ────────────────────────────────────────────────────────────────
-- tat_trabajo_favorito
-- ────────────────────────────────────────────────────────────────
ALTER TABLE tat_trabajo_favorito
    ADD COLUMN IF NOT EXISTS creationuser          VARCHAR(25) NOT NULL DEFAULT 'SYSTEM',
    ADD COLUMN IF NOT EXISTS creationtimestamp     TIMESTAMP   NOT NULL DEFAULT NOW(),
    ADD COLUMN IF NOT EXISTS modificationuser      VARCHAR(25),
    ADD COLUMN IF NOT EXISTS modificationtimestamp TIMESTAMP,
    ADD COLUMN IF NOT EXISTS status                VARCHAR     NOT NULL DEFAULT 'true';

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns
               WHERE table_name='tat_trabajo_favorito' AND column_name='created_at') THEN
        UPDATE tat_trabajo_favorito
           SET creationtimestamp = created_at;
    END IF;
END $$;

ALTER TABLE tat_trabajo_favorito
    DROP COLUMN IF EXISTS created_at,
    DROP COLUMN IF EXISTS updated_at,
    DROP COLUMN IF EXISTS active;

-- ────────────────────────────────────────────────────────────────
-- tat_solicitud_tatuador  (solicitud_status = dominio; status = auditoría)
-- ────────────────────────────────────────────────────────────────
ALTER TABLE tat_solicitud_tatuador
    ADD COLUMN IF NOT EXISTS creationuser          VARCHAR(25) NOT NULL DEFAULT 'SYSTEM',
    ADD COLUMN IF NOT EXISTS creationtimestamp     TIMESTAMP   NOT NULL DEFAULT NOW(),
    ADD COLUMN IF NOT EXISTS modificationuser      VARCHAR(25),
    ADD COLUMN IF NOT EXISTS modificationtimestamp TIMESTAMP,
    ADD COLUMN IF NOT EXISTS status                VARCHAR     NOT NULL DEFAULT 'true';

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns
               WHERE table_name='tat_solicitud_tatuador' AND column_name='created_at') THEN
        UPDATE tat_solicitud_tatuador
           SET creationtimestamp     = created_at,
               modificationtimestamp = updated_at;
    END IF;
END $$;

ALTER TABLE tat_solicitud_tatuador
    DROP COLUMN IF EXISTS created_at,
    DROP COLUMN IF EXISTS updated_at,
    DROP COLUMN IF EXISTS active;

-- ────────────────────────────────────────────────────────────────
-- tat_instagram_token
-- ────────────────────────────────────────────────────────────────
ALTER TABLE tat_instagram_token
    ADD COLUMN IF NOT EXISTS creationuser          VARCHAR(25) NOT NULL DEFAULT 'SYSTEM',
    ADD COLUMN IF NOT EXISTS creationtimestamp     TIMESTAMP   NOT NULL DEFAULT NOW(),
    ADD COLUMN IF NOT EXISTS modificationuser      VARCHAR(25),
    ADD COLUMN IF NOT EXISTS modificationtimestamp TIMESTAMP,
    ADD COLUMN IF NOT EXISTS status                VARCHAR     NOT NULL DEFAULT 'true';

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns
               WHERE table_name='tat_instagram_token' AND column_name='created_at') THEN
        UPDATE tat_instagram_token
           SET creationtimestamp     = created_at,
               modificationtimestamp = updated_at;
    END IF;
END $$;

ALTER TABLE tat_instagram_token
    DROP COLUMN IF EXISTS created_at,
    DROP COLUMN IF EXISTS updated_at,
    DROP COLUMN IF EXISTS active;

-- ────────────────────────────────────────────────────────────────
-- tat_horario_habitual
-- ────────────────────────────────────────────────────────────────
ALTER TABLE tat_horario_habitual
    ADD COLUMN IF NOT EXISTS creationuser          VARCHAR(25) NOT NULL DEFAULT 'SYSTEM',
    ADD COLUMN IF NOT EXISTS creationtimestamp     TIMESTAMP   NOT NULL DEFAULT NOW(),
    ADD COLUMN IF NOT EXISTS modificationuser      VARCHAR(25),
    ADD COLUMN IF NOT EXISTS modificationtimestamp TIMESTAMP,
    ADD COLUMN IF NOT EXISTS status                VARCHAR     NOT NULL DEFAULT 'true';

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns
               WHERE table_name='tat_horario_habitual' AND column_name='created_at') THEN
        UPDATE tat_horario_habitual
           SET creationtimestamp = created_at;
    END IF;
END $$;

ALTER TABLE tat_horario_habitual
    DROP COLUMN IF EXISTS created_at,
    DROP COLUMN IF EXISTS updated_at,
    DROP COLUMN IF EXISTS active;
