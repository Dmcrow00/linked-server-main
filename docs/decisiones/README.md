# Decisiones de arquitectura (ADR)

Un archivo por decisión: `ADR-NNNN-titulo-corto.md` (plantilla en
`dmc00-deploy-templates/templates/docs-proyecto/docs/decisiones/ADR-0000-plantilla.md`). Nunca se borra
una; si se revierte, se crea otra que la reemplaza.

Las decisiones comunes a todos los proyectos están en `dmc00-workspace/docs/decisiones/` y las de las
librerías en `dmc00-core/docs/decisiones/` (en particular ADR-0003, historial de migraciones de core-swe, y
ADR-0004, perfil propio fuera de `swe_usrapl`, que linked aplica con `tat_perfil_usuario`).

| ADR | Título | Estado | Fecha |
|---|---|---|---|
| [0001](ADR-0001-rutas-locales-de-uploads.md) | Las imágenes de `/uploads` del backend Node no se sirven ni se migran solas | Propuesta | 2026-10-15 |
| [0002](ADR-0002-v1-y-v11-quedan-como-estan-aplicadas.md) | V1 y V11 quedan como están aplicadas en las bases | Propuesta | 2026-10-15 |
| [0003](ADR-0003-admin-semilla-sin-contrasena.md) | El admin semilla queda sin contraseña utilizable | Propuesta | 2026-10-15 |
