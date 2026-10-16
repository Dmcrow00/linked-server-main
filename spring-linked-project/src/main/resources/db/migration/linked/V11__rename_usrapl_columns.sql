-- ================================================================
-- V11: Alinear columnas de swe_usrapl con los nombres de la entidad
--      nombre          → realname
--      apellido        → surname
--      nombre_usuario  → username  (NOT NULL + unique constraint)
--      fecha_nacimiento → birth_date  (copiar datos, columna ya existe de V9)
--      foto_perfil      → profile_photo (copiar datos, columna ya existe de V9)
-- ================================================================

-- ── 1. Agregar columnas con nombres correctos ────────────────────
ALTER TABLE swe_usrapl
    ADD COLUMN IF NOT EXISTS realname VARCHAR(25),
    ADD COLUMN IF NOT EXISTS surname  VARCHAR(25),
    ADD COLUMN IF NOT EXISTS username VARCHAR(25);

-- ── 2 a 4. Copiar datos de las columnas del backend Node ─────────
-- Las columnas viejas solo existen en una base migrada desde Node. En una base nueva V1 ya
-- crea las definitivas y no hay nada que copiar: cada UPDATE corre solo si su columna existe
-- (PL/pgSQL no valida las columnas de una sentencia hasta ejecutarla).
DO $$
DECLARE
    columnas_node TEXT[];
BEGIN
    SELECT array_agg(column_name::TEXT) INTO columnas_node
    FROM information_schema.columns
    WHERE table_schema = current_schema()
      AND table_name = 'swe_usrapl'
      AND column_name IN ('nombre', 'apellido', 'nombre_usuario', 'fecha_nacimiento', 'foto_perfil');

    IF 'nombre' = ANY (columnas_node) THEN
        UPDATE swe_usrapl SET realname = nombre WHERE realname IS NULL AND nombre IS NOT NULL;
    END IF;
    IF 'apellido' = ANY (columnas_node) THEN
        UPDATE swe_usrapl SET surname = apellido WHERE surname IS NULL AND apellido IS NOT NULL;
    END IF;
    IF 'nombre_usuario' = ANY (columnas_node) THEN
        UPDATE swe_usrapl SET username = nombre_usuario WHERE username IS NULL AND nombre_usuario IS NOT NULL;
    END IF;

    -- birth_date desde fecha_nacimiento (para filas con el DEFAULT '1990-01-01')
    IF 'fecha_nacimiento' = ANY (columnas_node) THEN
        UPDATE swe_usrapl
        SET birth_date = fecha_nacimiento
        WHERE fecha_nacimiento IS NOT NULL
          AND fecha_nacimiento <> '1990-01-01'
          AND birth_date = '1990-01-01';
    END IF;

    -- profile_photo desde foto_perfil
    IF 'foto_perfil' = ANY (columnas_node) THEN
        UPDATE swe_usrapl
        SET profile_photo = foto_perfil
        WHERE foto_perfil IS NOT NULL AND profile_photo IS NULL;
    END IF;
END $$;

-- ── 5. Asegurar que username no tenga NULLs antes del NOT NULL ───
UPDATE swe_usrapl SET username = 'user_' || id WHERE username IS NULL;

-- ── 6. Establecer username NOT NULL ─────────────────────────────
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'swe_usrapl' AND column_name = 'username' AND is_nullable = 'YES'
    ) THEN
        ALTER TABLE swe_usrapl ALTER COLUMN username SET NOT NULL;
    END IF;
END $$;

-- ── 7. Renombrar unique constraint nombre_usuario → username ──────
DO $$
BEGIN
    -- Eliminar constraint viejo si existe
    IF EXISTS (
        SELECT 1 FROM information_schema.table_constraints
        WHERE table_name = 'swe_usrapl' AND constraint_name = 'swe_usrapl_nombre_usuario_key'
    ) THEN
        ALTER TABLE swe_usrapl DROP CONSTRAINT swe_usrapl_nombre_usuario_key;
    END IF;

    -- Agregar constraint nuevo con el nombre que espera la entidad
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints
        WHERE table_name = 'swe_usrapl' AND constraint_name = 'swe_usrapl_username_key'
    ) THEN
        ALTER TABLE swe_usrapl ADD CONSTRAINT swe_usrapl_username_key UNIQUE (username);
    END IF;
END $$;
