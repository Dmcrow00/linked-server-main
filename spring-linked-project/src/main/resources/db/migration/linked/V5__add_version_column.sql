-- ================================================================
-- V5: Agregar columna version (optimistic locking) a tablas que
--     extienden BaseEntity y no la recibieron en V3/V4
-- ================================================================

ALTER TABLE tat_especialidad
    ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;

ALTER TABLE tat_trabajo
    ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;

ALTER TABLE tat_turno_sesion
    ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;

ALTER TABLE hd_support_ticket
    ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;

ALTER TABLE tat_imagen_ref
    ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;

ALTER TABLE tat_trabajo_favorito
    ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;
