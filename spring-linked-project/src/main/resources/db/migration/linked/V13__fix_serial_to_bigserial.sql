-- ================================================================
-- V13: Convertir columnas id de INTEGER (SERIAL) a BIGINT (BIGSERIAL)
--      en tablas donde el DDL original usó SERIAL en vez de BIGSERIAL.
--      Hibernate 6 con Long/BIGINT rechaza columnas INTEGER.
-- ================================================================

CREATE OR REPLACE FUNCTION fix_id_to_bigint(tbl TEXT) RETURNS VOID AS $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name  = tbl
          AND column_name = 'id'
          AND data_type   = 'integer'
    ) THEN
        EXECUTE format('ALTER TABLE %I ALTER COLUMN id TYPE BIGINT', tbl);
    END IF;
END;
$$ LANGUAGE plpgsql;

SELECT fix_id_to_bigint('swe_rolapl');
SELECT fix_id_to_bigint('swe_usrapl');
SELECT fix_id_to_bigint('swe_usrauth');
SELECT fix_id_to_bigint('swe_usrrolapl');
SELECT fix_id_to_bigint('swe_itemmenu');
SELECT fix_id_to_bigint('swe_accmodapl');
SELECT fix_id_to_bigint('tat_especialidad');
SELECT fix_id_to_bigint('tat_trabajo');
SELECT fix_id_to_bigint('tat_trabajo_foto');
SELECT fix_id_to_bigint('tat_trabajo_favorito');
SELECT fix_id_to_bigint('tat_turno_sesion');
SELECT fix_id_to_bigint('tat_turno_mensajes');
SELECT fix_id_to_bigint('tat_imagen_ref');
SELECT fix_id_to_bigint('tat_horario_habitual');
SELECT fix_id_to_bigint('tat_instagram_token');
SELECT fix_id_to_bigint('tat_solicitud_tatuador');
SELECT fix_id_to_bigint('hd_support_ticket');

DROP FUNCTION IF EXISTS fix_id_to_bigint(TEXT);
