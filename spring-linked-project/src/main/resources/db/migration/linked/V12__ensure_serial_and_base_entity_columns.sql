-- ================================================================
-- V12: Garantizar BIGSERIAL (secuencia) en columna id de todas las
--      tablas mapeadas con @GeneratedValue(IDENTITY), y agregar las
--      columnas de BaseEntity que faltan en tablas creadas con DDL
--      antiguo o incompleto.
-- ================================================================

-- ────────────────────────────────────────────────────────────────
-- FUNCIÓN AUXILIAR: asegura secuencia en la columna id
-- ────────────────────────────────────────────────────────────────
CREATE OR REPLACE FUNCTION ensure_bigserial(tbl TEXT) RETURNS VOID AS $$
DECLARE
    seq_name TEXT;
BEGIN
    seq_name := pg_get_serial_sequence(tbl, 'id');
    IF seq_name IS NULL THEN
        EXECUTE format('CREATE SEQUENCE IF NOT EXISTS %I_id_seq', tbl);
        EXECUTE format(
            'SELECT setval(%L, COALESCE((SELECT MAX(id) FROM %I), 0) + 1, false)',
            tbl || '_id_seq', tbl
        );
        EXECUTE format(
            'ALTER TABLE %I ALTER COLUMN id SET DEFAULT nextval(%L)',
            tbl, tbl || '_id_seq'
        );
        EXECUTE format(
            'ALTER SEQUENCE %I_id_seq OWNED BY %I.id',
            tbl, tbl
        );
    END IF;
END;
$$ LANGUAGE plpgsql;

-- ── Aplicar a todas las tablas con @GeneratedValue(IDENTITY) ────
SELECT ensure_bigserial('swe_rolapl');
SELECT ensure_bigserial('swe_usrapl');
SELECT ensure_bigserial('swe_usrauth');
SELECT ensure_bigserial('swe_usrrolapl');
SELECT ensure_bigserial('swe_itemmenu');
SELECT ensure_bigserial('swe_accmodapl');
SELECT ensure_bigserial('tat_especialidad');
SELECT ensure_bigserial('tat_trabajo');
SELECT ensure_bigserial('tat_trabajo_foto');
SELECT ensure_bigserial('tat_trabajo_favorito');
SELECT ensure_bigserial('tat_turno_sesion');
SELECT ensure_bigserial('tat_turno_mensajes');
SELECT ensure_bigserial('tat_imagen_ref');
SELECT ensure_bigserial('tat_horario_habitual');
SELECT ensure_bigserial('tat_instagram_token');
SELECT ensure_bigserial('tat_solicitud_tatuador');
SELECT ensure_bigserial('hd_support_ticket');

DROP FUNCTION IF EXISTS ensure_bigserial(TEXT);

-- ────────────────────────────────────────────────────────────────
-- tat_trabajo_foto: agrega columnas de BaseEntity faltantes
-- ────────────────────────────────────────────────────────────────
ALTER TABLE tat_trabajo_foto
    ADD COLUMN IF NOT EXISTS version               BIGINT      NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS creationuser          VARCHAR(25) NOT NULL DEFAULT 'SYSTEM',
    ADD COLUMN IF NOT EXISTS creationtimestamp     TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN IF NOT EXISTS modificationuser      VARCHAR(25),
    ADD COLUMN IF NOT EXISTS modificationtimestamp TIMESTAMP,
    ADD COLUMN IF NOT EXISTS status                VARCHAR     NOT NULL DEFAULT 'true';

-- ────────────────────────────────────────────────────────────────
-- tat_turno_mensajes: agrega columnas de BaseEntity faltantes
-- ────────────────────────────────────────────────────────────────
ALTER TABLE tat_turno_mensajes
    ADD COLUMN IF NOT EXISTS version               BIGINT      NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS creationuser          VARCHAR(25) NOT NULL DEFAULT 'SYSTEM',
    ADD COLUMN IF NOT EXISTS creationtimestamp     TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN IF NOT EXISTS modificationuser      VARCHAR(25),
    ADD COLUMN IF NOT EXISTS modificationtimestamp TIMESTAMP,
    ADD COLUMN IF NOT EXISTS status                VARCHAR     NOT NULL DEFAULT 'true';

-- ────────────────────────────────────────────────────────────────
-- tat_horario_habitual: agrega columnas de BaseEntity faltantes
-- ────────────────────────────────────────────────────────────────
ALTER TABLE tat_horario_habitual
    ADD COLUMN IF NOT EXISTS version               BIGINT      NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS modificationuser      VARCHAR(25),
    ADD COLUMN IF NOT EXISTS modificationtimestamp TIMESTAMP,
    ADD COLUMN IF NOT EXISTS status                VARCHAR     NOT NULL DEFAULT 'true';

-- ────────────────────────────────────────────────────────────────
-- tat_instagram_token: agrega columnas de BaseEntity faltantes
-- ────────────────────────────────────────────────────────────────
ALTER TABLE tat_instagram_token
    ADD COLUMN IF NOT EXISTS version               BIGINT      NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS creationuser          VARCHAR(25) NOT NULL DEFAULT 'SYSTEM',
    ADD COLUMN IF NOT EXISTS modificationuser      VARCHAR(25),
    ADD COLUMN IF NOT EXISTS status                VARCHAR     NOT NULL DEFAULT 'true';

-- ────────────────────────────────────────────────────────────────
-- tat_trabajo_favorito: agrega columnas de BaseEntity faltantes
-- ────────────────────────────────────────────────────────────────
ALTER TABLE tat_trabajo_favorito
    ADD COLUMN IF NOT EXISTS creationuser          VARCHAR(25) NOT NULL DEFAULT 'SYSTEM',
    ADD COLUMN IF NOT EXISTS modificationuser      VARCHAR(25),
    ADD COLUMN IF NOT EXISTS modificationtimestamp TIMESTAMP,
    ADD COLUMN IF NOT EXISTS status                VARCHAR     NOT NULL DEFAULT 'true';

-- ────────────────────────────────────────────────────────────────
-- tat_solicitud_tatuador: renombrar status → solicitud_status y
--   agregar status de BaseEntity (VARCHAR "true"/"false")
-- ────────────────────────────────────────────────────────────────
DO $$
BEGIN
    -- Renombrar columna status → solicitud_status si todavía no se hizo
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'tat_solicitud_tatuador' AND column_name = 'status'
    ) AND NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'tat_solicitud_tatuador' AND column_name = 'solicitud_status'
    ) THEN
        ALTER TABLE tat_solicitud_tatuador RENAME COLUMN status TO solicitud_status;
    END IF;
END $$;

ALTER TABLE tat_solicitud_tatuador
    ADD COLUMN IF NOT EXISTS version               BIGINT      NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS creationuser          VARCHAR(25) NOT NULL DEFAULT 'SYSTEM',
    ADD COLUMN IF NOT EXISTS modificationuser      VARCHAR(25),
    ADD COLUMN IF NOT EXISTS status                VARCHAR     NOT NULL DEFAULT 'true';
