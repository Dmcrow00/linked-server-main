-- ================================================================
-- V17: Alinear con las entidades dos columnas que V2 creó distinto
--      tat_turno_mensajes.mensaje      → texto    (TurnoMensaje.texto)
--      tat_horario_habitual.dia_semana SMALLINT → INTEGER (HorarioHabitual.diaSemana es int)
--
-- Las bases que ya corrían el backend Spring se ajustaron a mano y tienen el estado final;
-- por eso cada paso mira antes si hace falta.
-- ================================================================

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = current_schema()
          AND table_name = 'tat_turno_mensajes' AND column_name = 'mensaje'
    ) THEN
        ALTER TABLE tat_turno_mensajes RENAME COLUMN mensaje TO texto;
    END IF;

    -- El CHECK (dia_semana BETWEEN 0 AND 6) y la restricción única se conservan.
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = current_schema()
          AND table_name = 'tat_horario_habitual' AND column_name = 'dia_semana'
          AND data_type = 'smallint'
    ) THEN
        ALTER TABLE tat_horario_habitual ALTER COLUMN dia_semana TYPE INTEGER;
    END IF;
END $$;
