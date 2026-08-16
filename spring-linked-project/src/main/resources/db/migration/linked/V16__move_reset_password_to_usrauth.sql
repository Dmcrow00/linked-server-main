-- ================================================================
-- V16: Mover reset_password_token y reset_password_expires
--      de swe_usrapl hacia swe_usrauth
-- ================================================================

-- ── 1. Agregar columnas en swe_usrauth ───────────────────────────
ALTER TABLE swe_usrauth
    ADD COLUMN IF NOT EXISTS reset_password_token   TEXT,
    ADD COLUMN IF NOT EXISTS reset_password_expires TIMESTAMP;

-- ── 2. Migrar datos existentes ────────────────────────────────────
UPDATE swe_usrauth auth
SET reset_password_token   = usr.reset_password_token,
    reset_password_expires = usr.reset_password_expires
FROM swe_usrapl usr
WHERE auth.id_usrapl = usr.id
  AND (usr.reset_password_token IS NOT NULL OR usr.reset_password_expires IS NOT NULL);

-- ── 3. Eliminar columnas de swe_usrapl ───────────────────────────
ALTER TABLE swe_usrapl
    DROP COLUMN IF EXISTS reset_password_token,
    DROP COLUMN IF EXISTS reset_password_expires;
