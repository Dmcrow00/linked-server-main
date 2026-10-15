-- ================================================================
-- V18: Corregir el hash de la contraseña del admin semilla
--
-- V1 cargaba un hash BCrypt que no correspondía a la contraseña documentada
-- (Admin@Linked2024), así que nadie podía entrar con ese usuario. V1 ya trae el hash
-- correcto para las bases nuevas; esto repara las bases donde V1 corrió con el viejo.
-- Solo reemplaza ese hash exacto: una contraseña que ya se haya cambiado no se toca.
-- ================================================================

UPDATE swe_usrauth
SET password = '$2a$10$oO1RxtgnEJkQ3EBXcTIqsOOqBsNLzBiRq.zR9buAZVQj1brdLPnYa'
WHERE password = '$2a$10$RSHX1.3YoGJuZN.xWfGCweRMEJ6EJv/aUBmFpDNv.LxKZ0cWQHMiC';
