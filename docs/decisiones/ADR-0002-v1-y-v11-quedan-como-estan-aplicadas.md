# ADR-0002: V1 y V11 quedan como están aplicadas en las bases

- Estado: Propuesta (se acepta al aprobar el PR que la introduce)
- Fecha: 2026-10-15
- Propone: Claude, dentro del plan de migración aprobado por el dueño (R. Ceballos)

## Contexto

- Regla del workspace: una migración aplicada no se edita; si hace falta, se corrige con una nueva.
- En `develop`, `V1__create_swe_tables.sql` y `V11__rename_usrapl_columns.sql` tienen una versión; en el
  resguardo del 2026-10-15 (`r.ceballos/WIP_20261015`) había otra, editada sin commitear. El plan pedía
  volver a la de `develop` y llevar el cambio a una migración nueva.
- Antes de hacerlo se comparó el checksum de Flyway de cada versión con el de las bases existentes
  (`flyway_schema_history`):

  | Base | V1 aplicada | V11 aplicada |
  |---|---|---|
  | `linked-main-db` (copia de la de Node, baseline en V1) | — | la del resguardo (-1752201251) |
  | `linked_portfolio` | la del resguardo (1562711135) | la del resguardo (-1752201251) |
  | versión de `develop` | -1862438246 | 236746606 |

- La V11 de `develop` copia de `swe_usrapl.nombre`, `apellido`, `nombre_usuario`..., columnas que no existen
  ni en una base nueva ni en la base de Node: no corre en ningún esquema conocido. La del resguardo hace
  cada copia solo si la columna existe.

## Opciones

1. **Volver a la versión de `develop`** y llevar el cambio a una migración nueva. Rompe `flyway validate`
   en las dos bases existentes, y una base nueva no se puede crear (la V11 falla antes de que corra
   cualquier migración nueva).
2. **Quedarse con la versión aplicada** (la del resguardo) y documentar por qué.

## Decisión

Opción 2. Las versiones de V1 y V11 que se versionan son las que están aplicadas. El objetivo de la regla
(que las bases existentes sigan validando) se cumple así y no con la otra opción.

Lo que la edición de V1 agregaba (el hash de una contraseña conocida para el admin semilla) no se
conserva: ver [ADR-0003](ADR-0003-admin-semilla-sin-contrasena.md).

## Consecuencias

- Una base que tuviera aplicada la V1 o la V11 de `develop` (no se conoce ninguna; la V11 no pudo haber
  corrido completa) necesita `flyway repair` antes de arrancar la versión nueva.
- `MigracionesIT#v11CopiaLasColumnasDeNodeSiExisten` prueba la V11 sobre una base con las columnas de Node.
- Verificado el 2026-10-15 sobre copias de `linked-main-db` y `linked_portfolio`: validan, migran hasta
  V20 y la app arranca.
