-- ================================================================
-- V6: Corregir tipos de columnas en hd_support_ticket
--     ticket_open: VARCHAR → BOOLEAN  (entidad espera boolean)
--     status:      BOOLEAN → VARCHAR   (BaseEntity espera VARCHAR)
-- ================================================================

-- Convertir ticket_open de VARCHAR a BOOLEAN si es necesario
-- Se debe eliminar el DEFAULT antes de cambiar el tipo porque 'true' (varchar) no castea automáticamente
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name  = 'hd_support_ticket'
          AND column_name = 'ticket_open'
          AND data_type   IN ('character varying', 'text', 'character')
    ) THEN
        ALTER TABLE hd_support_ticket ALTER COLUMN ticket_open DROP DEFAULT;
        ALTER TABLE hd_support_ticket
            ALTER COLUMN ticket_open TYPE BOOLEAN
            USING (ticket_open = 'true');
        ALTER TABLE hd_support_ticket ALTER COLUMN ticket_open SET DEFAULT TRUE;
    END IF;
END $$;

-- Convertir status de BOOLEAN a VARCHAR si es necesario
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name  = 'hd_support_ticket'
          AND column_name = 'status'
          AND data_type   = 'boolean'
    ) THEN
        ALTER TABLE hd_support_ticket ALTER COLUMN status DROP DEFAULT;
        ALTER TABLE hd_support_ticket
            ALTER COLUMN status TYPE VARCHAR
            USING CASE WHEN status THEN 'true' ELSE 'false' END;
        ALTER TABLE hd_support_ticket ALTER COLUMN status SET DEFAULT 'true';
    END IF;
END $$;
