-- ================================================================
-- V8: Agregar columna version (optimistic locking) a tablas swe_*
--     que fueron creadas antes de que V1 incluyera dicha columna
-- ================================================================

ALTER TABLE swe_rolapl      ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE swe_usrapl      ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE swe_usrauth     ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE swe_usrrolapl   ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE swe_itemmenu    ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;
