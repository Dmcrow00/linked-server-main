-- ============================================================
-- V1__create_swe_tables.sql
-- Esquema swe_* para autenticacion, roles, permisos y menu
-- Adaptado para LINKED: 4 roles (Administrador, Cliente, Tatuador, Moderador)
-- ============================================================

-- ────────────────────────────────────────────────────────────
-- 1. swe_rolapl
-- ────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS swe_rolapl (
    id                      BIGSERIAL       NOT NULL,
    description             VARCHAR(50)     NOT NULL,
    creationuser            VARCHAR(25)     NOT NULL DEFAULT 'SYSTEM',
    creationtimestamp       TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modificationuser        VARCHAR(25),
    modificationtimestamp   TIMESTAMP,
    status                  VARCHAR         NOT NULL DEFAULT 'true',
    version                 BIGINT          NOT NULL DEFAULT 0,
    CONSTRAINT swe_rolapl_pkey PRIMARY KEY (id),
    CONSTRAINT swe_rolapl_description_key UNIQUE (description)
);
CREATE INDEX IF NOT EXISTS idx_rolapl_status ON swe_rolapl(status);

-- ────────────────────────────────────────────────────────────
-- 2. swe_usrapl
-- ────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS swe_usrapl (
    id                      BIGSERIAL       NOT NULL,
    realname                VARCHAR(25),
    surname                 VARCHAR(25),
    username                VARCHAR(25)     NOT NULL,
    email                   VARCHAR(255)    NOT NULL,
    birth_date              DATE            NOT NULL,
    profile_photo           TEXT,
    estudio                 VARCHAR(100),
    fecha_inicio_actividad  DATE,
    delete_date             TIMESTAMP,
    reset_password_token    TEXT,
    reset_password_expires  TIMESTAMP,
    localidad               VARCHAR(255),
    lat                     DOUBLE PRECISION,
    lng                     DOUBLE PRECISION,
    instagram_handle        VARCHAR(100),
    creationuser            VARCHAR(25)     NOT NULL DEFAULT 'SYSTEM',
    creationtimestamp       TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modificationuser        VARCHAR(25),
    modificationtimestamp   TIMESTAMP,
    status                  VARCHAR         NOT NULL DEFAULT 'true',
    version                 BIGINT          NOT NULL DEFAULT 0,
    CONSTRAINT swe_usrapl_pkey          PRIMARY KEY (id),
    CONSTRAINT swe_usrapl_email_key     UNIQUE (email),
    CONSTRAINT swe_usrapl_username_key  UNIQUE (username)
);
CREATE INDEX IF NOT EXISTS idx_usrapl_username  ON swe_usrapl(username);
CREATE INDEX IF NOT EXISTS idx_usrapl_email     ON swe_usrapl(email);
CREATE INDEX IF NOT EXISTS idx_usrapl_status    ON swe_usrapl(status);
CREATE INDEX IF NOT EXISTS idx_usrapl_location  ON swe_usrapl(lat, lng);

-- ────────────────────────────────────────────────────────────
-- 3. swe_usrauth
-- ────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS swe_usrauth (
    id                      BIGSERIAL       NOT NULL,
    id_usrapl               BIGINT          NOT NULL,
    password                VARCHAR(150)    NOT NULL,
    creationuser            VARCHAR(25)     NOT NULL DEFAULT 'SYSTEM',
    creationtimestamp       TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modificationtimestamp   TIMESTAMP,
    version                 BIGINT          NOT NULL DEFAULT 0,
    CONSTRAINT swe_usrauth_pkey             PRIMARY KEY (id),
    CONSTRAINT swe_usrauth_id_usrapl_key    UNIQUE (id_usrapl),
    CONSTRAINT swe_usrauth_id_usrapl_fkey
        FOREIGN KEY (id_usrapl) REFERENCES swe_usrapl(id)
        ON DELETE CASCADE ON UPDATE CASCADE
);

-- ────────────────────────────────────────────────────────────
-- 4. swe_usrrolapl
-- ────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS swe_usrrolapl (
    id                      BIGSERIAL       NOT NULL,
    id_usrapl               BIGINT          NOT NULL,
    id_rolapl               BIGINT          NOT NULL,
    status                  VARCHAR         NOT NULL DEFAULT 'true',
    creationuser            VARCHAR(25)     NOT NULL DEFAULT 'SYSTEM',
    creationtimestamp       TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version                 BIGINT          NOT NULL DEFAULT 0,
    CONSTRAINT swe_usrrolapl_pkey   PRIMARY KEY (id),
    CONSTRAINT swe_usrrolapl_unique UNIQUE (id_usrapl, id_rolapl),
    CONSTRAINT swe_usrrolapl_id_usrapl_fkey
        FOREIGN KEY (id_usrapl) REFERENCES swe_usrapl(id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT swe_usrrolapl_id_rolapl_fkey
        FOREIGN KEY (id_rolapl) REFERENCES swe_rolapl(id)
        ON DELETE RESTRICT ON UPDATE CASCADE
);
CREATE INDEX IF NOT EXISTS idx_usrrolapl_usr ON swe_usrrolapl(id_usrapl);
CREATE INDEX IF NOT EXISTS idx_usrrolapl_rol ON swe_usrrolapl(id_rolapl);

-- ────────────────────────────────────────────────────────────
-- 5. swe_itemmenu
-- ────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS swe_itemmenu (
    id                      BIGSERIAL       NOT NULL,
    id_supitemmenu          BIGINT,
    title                   VARCHAR(100)    NOT NULL,
    icon                    VARCHAR(100),
    route                   VARCHAR(255),
    order_number            INT,
    roles_permitidos        VARCHAR(500),
    creationuser            VARCHAR(25)     NOT NULL DEFAULT 'SYSTEM',
    creationtimestamp       TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modificationuser        VARCHAR(25),
    modificationtimestamp   TIMESTAMP,
    status                  VARCHAR         NOT NULL DEFAULT 'true',
    version                 BIGINT          NOT NULL DEFAULT 0,
    CONSTRAINT swe_itemmenu_pkey PRIMARY KEY (id),
    CONSTRAINT swe_itemmenu_id_supitemmenu_fkey
        FOREIGN KEY (id_supitemmenu) REFERENCES swe_itemmenu(id)
        ON DELETE SET NULL ON UPDATE CASCADE
);
CREATE INDEX IF NOT EXISTS idx_itemmenu_parent ON swe_itemmenu(id_supitemmenu);
CREATE INDEX IF NOT EXISTS idx_itemmenu_status ON swe_itemmenu(status);

-- ────────────────────────────────────────────────────────────
-- 6. swe_accmodapl
-- ────────────────────────────────────────────────────────────
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

-- ────────────────────────────────────────────────────────────
-- Datos semilla
-- ────────────────────────────────────────────────────────────

-- Roles de LINKED
INSERT INTO swe_rolapl (description, creationuser, status)
VALUES
    ('Administrador', 'SYSTEM', 'true'),
    ('Cliente',       'SYSTEM', 'true'),
    ('Tatuador',      'SYSTEM', 'true'),
    ('Moderador',     'SYSTEM', 'true')
ON CONFLICT (description) DO NOTHING;

-- Usuario administrador inicial
-- Contrasena: Admin@Linked2024  (BCrypt strength=10)
-- CAMBIAR ANTES DEL PRIMER DEPLOY A PRODUCCION
INSERT INTO swe_usrapl (username, email, birth_date, realname, surname, creationuser, status)
VALUES ('admin', 'admin@linked-app.com', '1990-01-01', 'Admin', 'LINKED', 'SYSTEM', 'true')
ON CONFLICT (username) DO NOTHING;

INSERT INTO swe_usrauth (id_usrapl, password, creationuser)
SELECT u.id, '$2a$10$RSHX1.3YoGJuZN.xWfGCweRMEJ6EJv/aUBmFpDNv.LxKZ0cWQHMiC', 'SYSTEM'
FROM swe_usrapl u
WHERE u.username = 'admin'
  AND NOT EXISTS (SELECT 1 FROM swe_usrauth a WHERE a.id_usrapl = u.id);

INSERT INTO swe_usrrolapl (id_usrapl, id_rolapl, creationuser)
SELECT u.id, r.id, 'SYSTEM'
FROM swe_usrapl u, swe_rolapl r
WHERE u.username = 'admin'
  AND r.description = 'Administrador'
  AND NOT EXISTS (
    SELECT 1 FROM swe_usrrolapl ur
    WHERE ur.id_usrapl = u.id AND ur.id_rolapl = r.id
  );
