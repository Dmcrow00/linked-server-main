-- ================================================================
-- V14: Convertir columnas FK de INTEGER (int4) a BIGINT (int8)
--      El DDL original usó INTEGER para claves foráneas; las entidades
--      mapean todas las relaciones con Long → BIGINT.
--      La conversión INTEGER→BIGINT es implícita en PostgreSQL.
-- ================================================================

CREATE OR REPLACE FUNCTION fix_col_to_bigint(tbl TEXT, col TEXT) RETURNS VOID AS $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name  = tbl
          AND column_name = col
          AND data_type   = 'integer'
    ) THEN
        EXECUTE format('ALTER TABLE %I ALTER COLUMN %I TYPE BIGINT', tbl, col);
    END IF;
END;
$$ LANGUAGE plpgsql;

-- ── swe_usrauth ─────────────────────────────────────────────────
SELECT fix_col_to_bigint('swe_usrauth', 'id_usrapl');

-- ── swe_usrrolapl ───────────────────────────────────────────────
SELECT fix_col_to_bigint('swe_usrrolapl', 'id_usrapl');
SELECT fix_col_to_bigint('swe_usrrolapl', 'id_rolapl');

-- ── swe_itemmenu ────────────────────────────────────────────────
SELECT fix_col_to_bigint('swe_itemmenu', 'id_supitemmenu');

-- ── swe_accmodapl ───────────────────────────────────────────────
SELECT fix_col_to_bigint('swe_accmodapl', 'id_rolapl');

-- ── tat_tatuador_especialidad ────────────────────────────────────
SELECT fix_col_to_bigint('tat_tatuador_especialidad', 'id_tatuador');
SELECT fix_col_to_bigint('tat_tatuador_especialidad', 'id_especialidad');

-- ── tat_trabajo ─────────────────────────────────────────────────
SELECT fix_col_to_bigint('tat_trabajo', 'id_tatuador');

-- ── tat_trabajo_foto ────────────────────────────────────────────
SELECT fix_col_to_bigint('tat_trabajo_foto', 'id_trabajo');

-- ── tat_trabajo_favorito ────────────────────────────────────────
SELECT fix_col_to_bigint('tat_trabajo_favorito', 'id_cliente');
SELECT fix_col_to_bigint('tat_trabajo_favorito', 'id_trabajo');

-- ── tat_turno_sesion ────────────────────────────────────────────
SELECT fix_col_to_bigint('tat_turno_sesion', 'id_cliente');

-- ── tat_turno_tatuador ──────────────────────────────────────────
SELECT fix_col_to_bigint('tat_turno_tatuador', 'id_tatuador');
SELECT fix_col_to_bigint('tat_turno_tatuador', 'id_turno_sesion');

-- ── tat_turno_mensajes ──────────────────────────────────────────
SELECT fix_col_to_bigint('tat_turno_mensajes', 'id_turno_sesion');
SELECT fix_col_to_bigint('tat_turno_mensajes', 'id_usuario_envia');

-- ── tat_imagen_ref ──────────────────────────────────────────────
SELECT fix_col_to_bigint('tat_imagen_ref', 'id_turno_sesion');

-- ── tat_horario_habitual ────────────────────────────────────────
SELECT fix_col_to_bigint('tat_horario_habitual', 'id_tatuador');

-- ── tat_instagram_token ─────────────────────────────────────────
SELECT fix_col_to_bigint('tat_instagram_token', 'id_tatuador');

-- ── tat_solicitud_tatuador ──────────────────────────────────────
SELECT fix_col_to_bigint('tat_solicitud_tatuador', 'id_user');

-- ── hd_usr_st ───────────────────────────────────────────────────
SELECT fix_col_to_bigint('hd_usr_st', 'id_ticket');
SELECT fix_col_to_bigint('hd_usr_st', 'id_user');

DROP FUNCTION IF EXISTS fix_col_to_bigint(TEXT, TEXT);
