# ADR-0001: Las imágenes de `/uploads` del backend Node no se sirven ni se migran solas

- Estado: Propuesta (se acepta al aprobar el PR que la introduce)
- Fecha: 2026-10-15
- Propone: Claude, dentro del plan de migración aprobado por el dueño (R. Ceballos)

## Contexto

- El backend Node guardaba algunas imágenes en su disco (`nodejs-legacy/uploads/`) y en la base dejaba la
  ruta relativa (`/uploads/users/...`, `/uploads/turnos/...`), que servía con `express.static`. Otras ya
  iban a Cloudinary.
- Spring guarda todo en Cloudinary y corre en contenedores sin disco persistente (Fly.io).
- La carpeta dejó de versionarse (linked-server-main#1); hay un respaldo fuera del repositorio
  (`D:\Desarrollo\dmc00-backups`). No se sabe todavía si son fotos de personas reales.
- Relevamiento de las bases locales el 2026-10-15: en la base de Node, 1 foto de perfil y 5 imágenes de
  referencia de turnos apuntan a `/uploads`; las bases que ya usaba Spring no tienen ninguna.

## Opciones

1. **Servir `/uploads` desde Spring** con un volumen. Necesita disco persistente y una ruta pública sin
   control de acceso para imágenes que pueden ser personales; mantiene dos lugares de almacenamiento.
2. **Subir los archivos a Cloudinary en la migración**. La migración necesitaría las credenciales y los
   archivos (que no están en el repo); publica en un CDN imágenes que nadie revisó.
3. **Dejar las filas como están**. El cliente muestra imágenes rotas para siempre y nadie sabe cuáles eran.
4. **Retirar las referencias y registrar cuáles eran** (V20): la foto de perfil queda vacía (el cliente
   muestra el avatar por defecto), las filas de imágenes de turnos y de trabajos se borran y su ruta queda
   en `tat_ruta_local_retirada`.

## Decisión

Opción 4. Spring no sirve archivos locales. `V20__retira_rutas_de_uploads_locales.sql` saca las
referencias y las registra; en una base creada por Spring no hace nada.

## Consecuencias

- Cuando la base de Node pase a Spring, esas 6 imágenes dejan de verse. Si alguna hay que recuperarla, se
  sube a mano a Cloudinary desde el respaldo y se vuelve a enlazar con la fila de `tat_ruta_local_retirada`
  (guarda la tabla, el id de la fila y el del turno o trabajo).
- Un trabajo que solo tuviera fotos locales quedaría sin fotos (hoy no hay ninguno).
- Pendiente del dueño: revisar si las imágenes del respaldo son de personas reales antes de subir alguna.
