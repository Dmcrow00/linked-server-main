-- ================================================================
-- V7: Crear tablas que pudieron haber quedado sin crear
--     por baseline-on-migrate o eliminación manual
-- ================================================================

CREATE TABLE IF NOT EXISTS swe_accmodapl (
    id                      BIGSERIAL       NOT NULL,
    id_rolapl               BIGINT          NOT NULL,
    project_name            VARCHAR(50)     NOT NULL,
    module_name             VARCHAR(100)    NOT NULL,
    resource_type           VARCHAR(20)     NOT NULL,
    resource_name           VARCHAR(150)    NOT NULL,
    permission_type         VARCHAR(20)     NOT NULL,
    creationuser            VARCHAR(25)     NOT NULL DEFAULT 'SYSTEM',
    creationtimestamp       TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modificationuser        VARCHAR(25),
    modificationtimestamp   TIMESTAMP,
    status                  VARCHAR         NOT NULL DEFAULT 'true',
    version                 BIGINT          NOT NULL DEFAULT 0,
    CONSTRAINT swe_accmodapl_pkey PRIMARY KEY (id),
    CONSTRAINT swe_accmodapl_unique_permission
        UNIQUE (id_rolapl, resource_type, resource_name, project_name, permission_type),
    CONSTRAINT swe_accmodapl_id_rolapl_fkey
        FOREIGN KEY (id_rolapl) REFERENCES swe_rolapl(id)
        ON DELETE RESTRICT ON UPDATE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_accmodapl_rol      ON swe_accmodapl(id_rolapl);
CREATE INDEX IF NOT EXISTS idx_accmodapl_resource ON swe_accmodapl(resource_name);
CREATE INDEX IF NOT EXISTS idx_accmodapl_status   ON swe_accmodapl(status);
