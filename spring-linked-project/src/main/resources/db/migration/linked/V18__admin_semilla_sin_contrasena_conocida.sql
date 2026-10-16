-- ================================================================
-- V18: El admin semilla queda sin contraseña utilizable
--
-- V1 siembra el usuario admin con un hash fijo. Las dos versiones de V1 que llegaron a
-- aplicarse cargaron hashes conocidos: uno que no corresponde a ninguna contraseña y otro
-- de una contraseña que quedó escrita en este repositorio, que es público. Ninguno de los
-- dos puede quedar en una base real.
--
-- Se reemplazan por un valor que no es un hash: ningún login lo valida. Para usar la
-- cuenta hay que asignarle una contraseña (docs/guia-entorno-local-y-despliegue.md, "Admin
-- inicial"); en desarrollo lo hace AdminSemillaDevRunner con ADMIN_DEV_PASSWORD.
-- Solo toca esos dos hashes exactos: una contraseña que ya se cambió no se pisa.
-- ================================================================

UPDATE swe_usrauth
SET password = 'SIN-CONTRASENA'
WHERE password IN (
    '$2a$10$RSHX1.3YoGJuZN.xWfGCweRMEJ6EJv/aUBmFpDNv.LxKZ0cWQHMiC',
    '$2a$10$oO1RxtgnEJkQ3EBXcTIqsOOqBsNLzBiRq.zR9buAZVQj1brdLPnYa'
);
