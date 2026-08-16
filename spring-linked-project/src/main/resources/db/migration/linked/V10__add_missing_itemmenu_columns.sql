-- ================================================================
-- V10: Agregar columnas faltantes en swe_itemmenu
--      title: NOT NULL en entidad → DEFAULT provisional para filas existentes
--      id_supitemmenu: FK self-referencial (parent item)
-- ================================================================

ALTER TABLE swe_itemmenu
    ADD COLUMN IF NOT EXISTS title          VARCHAR(100) NOT NULL DEFAULT 'menu',
    ADD COLUMN IF NOT EXISTS id_supitemmenu BIGINT;

-- Agregar FK constraint si no existe
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints
        WHERE table_name       = 'swe_itemmenu'
          AND constraint_name  = 'swe_itemmenu_id_supitemmenu_fkey'
          AND constraint_type  = 'FOREIGN KEY'
    ) THEN
        ALTER TABLE swe_itemmenu
            ADD CONSTRAINT swe_itemmenu_id_supitemmenu_fkey
            FOREIGN KEY (id_supitemmenu) REFERENCES swe_itemmenu(id)
            ON DELETE SET NULL ON UPDATE CASCADE;
    END IF;
END $$;
