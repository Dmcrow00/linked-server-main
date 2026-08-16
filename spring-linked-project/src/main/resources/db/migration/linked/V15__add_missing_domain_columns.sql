-- ================================================================
-- V15: Agregar columnas de dominio faltantes en tablas tat_*
--      cuyo DDL original fue creado antes de que se agregaran
--      estos campos a las entidades o a BaseEntity.
-- ================================================================

-- ── tat_turno_mensajes: leido (dominio) ─────────────────────────
-- TurnoMensaje.leido = boolean NOT NULL, default false
ALTER TABLE tat_turno_mensajes
    ADD COLUMN IF NOT EXISTS leido BOOLEAN NOT NULL DEFAULT FALSE;

-- ── tat_turno_sesion: status (BaseEntity) ───────────────────────
-- V2 incluía version/creationuser/etc pero omitió status
ALTER TABLE tat_turno_sesion
    ADD COLUMN IF NOT EXISTS status VARCHAR NOT NULL DEFAULT 'true';

-- ── tat_imagen_ref: status (BaseEntity) ─────────────────────────
-- Ídem: V2 omitió status en esta tabla
ALTER TABLE tat_imagen_ref
    ADD COLUMN IF NOT EXISTS status VARCHAR NOT NULL DEFAULT 'true';
