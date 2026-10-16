# ADR-0003: El admin semilla queda sin contraseña utilizable

- Estado: Propuesta (se acepta al aprobar el PR que la introduce)
- Fecha: 2026-10-15
- Propone: Claude, dentro del plan de migración aprobado por el dueño (R. Ceballos)

## Contexto

- `V1__create_swe_tables.sql` siembra el usuario `admin` con un hash BCrypt fijo. Las dos versiones de V1
  que llegaron a aplicarse (ADR-0002) cargaron hashes conocidos: uno que no corresponde a ninguna
  contraseña (nadie podía entrar) y otro de una contraseña que quedó escrita en el repositorio, que es
  público.
- El resguardo del 2026-10-15 traía una V18 que pasaba el primer hash al segundo, para poder entrar en
  desarrollo. En una base nueva de producción eso deja al administrador con una contraseña pública. Esa
  V18 no estaba aplicada en ninguna base conocida.
- shop-core resolvió el mismo problema con un runner que solo corre en el perfil dev (commit 266a46f).

## Opciones

1. **V18 con el hash de la contraseña conocida** (la del resguardo): cómodo en desarrollo, inseguro en
   cualquier base real.
2. **V18 que deja la cuenta sin contraseña utilizable** y, en dev, un runner que le asigna una contraseña
   local tomada de una variable de entorno.
3. **Borrar el usuario semilla**: rompe a quien ya lo use y no deja forma de administrar una base nueva.

## Decisión

Opción 2.

- `V18__admin_semilla_sin_contrasena_conocida.sql` reemplaza cualquiera de los dos hashes conocidos por
  `SIN-CONTRASENA`, que no es un hash: ningún login lo valida. Una contraseña que ya se cambió no se toca.
- `AdminSemillaDevRunner` (solo perfil `dev`) le pone la contraseña de `ADMIN_DEV_PASSWORD` si la cuenta
  sigue sin contraseña. Sin la variable no hace nada.
- En producción el admin se habilita con "olvidé mi contraseña" (guía de entorno, "Admin inicial").

## Consecuencias

- Quien usaba el admin semilla en su base local con la contraseña publicada tiene que definir
  `ADMIN_DEV_PASSWORD` en su `.env`.
- La contraseña publicada sigue en el historial de git y en un comentario de V1 (que no se puede editar):
  hay que considerarla comprometida y no usarla en ningún entorno (pendiente del dueño en el plan del
  workspace).
