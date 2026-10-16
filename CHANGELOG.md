# Changelog

Formato [Keep a Changelog](https://keepachangelog.com/es-ES/1.1.0/); versionado
[SemVer](https://semver.org/lang/es/). Cubre el backend Spring (`spring-linked-project/`); la versión
Node (`nodejs-legacy/`) no tiene historial de versiones.

## [Sin publicar]

### Agregado

- Sincronización de Instagram cada 2 h (`INSTAGRAM_SYNC_INTERVAL`), como el scheduler de Node: renueva los
  tokens que vencen en menos de 30 días, desvincula los vencidos (salvo en dev) e importa las publicaciones.
  También se sincroniza al iniciar sesión un tatuador y al terminar de vincular la cuenta.
- La sincronización recorre todas las páginas de publicaciones, importa todas las imágenes de cada carrusel
  y da de baja los trabajos cuya publicación se borró en Instagram. `POST /api/instagram/sync` devuelve
  `{nuevos, eliminados, omitidos}`.
- `PUT /api/users/{id}` permite al administrador editar el perfil de cualquier usuario (reemplaza al
  `PUT /api/users/updateUser/:id` de Node).
- Perfil público de un tatuador sin sesión: `GET /api/users/{id}/publico`.
- Tabla `tat_perfil_usuario` (V19) con los datos propios de linked; las columnas viejas de `swe_usrapl`
  quedan hasta la versión siguiente.
- `AdminSemillaDevRunner`: en dev, el admin semilla toma la contraseña de `ADMIN_DEV_PASSWORD`.
- Tests de integración contra PostgreSQL en Testcontainers (autenticación, usuarios, turnos, Instagram y
  migraciones sobre datos existentes), reglas de arquitectura con ArchUnit, Dockerfile, CI y controles de
  seguridad (Gitleaks, Semgrep, dependencias, Trivy).
- Documentación: técnica, funcional, entorno y despliegue, paridad con Node, diagramas y ADR.

### Cambiado

- Java 25 y Spring Boot 4.1 sobre `dmc00-parent`. Respuestas, errores, JWT, RSA, contraseñas, CORS,
  reCAPTCHA, usuarios, roles, login y menú vienen de `dmc00-core` (`core-web`, `core-data`,
  `core-security`, `core-swe`).
- Cada endpoint declara su permiso con `@PreAuthorize`. Ahora exigen rol los endpoints de escritura que en
  Node no lo exigían (ver la tabla de paridad).
- `PUT /api/users/{id}` y `PATCH /api/users/{id}/profile-photo`: solo el propio usuario o un administrador.
- `PATCH /api/users/{id}/profile-photo` responde 403 `FOTO_SINCRONIZADA_CON_INSTAGRAM` si la cuenta tiene
  Instagram vinculado, como en Node.
- Turnos: un turno Completada o Cancelada ya no cambia de estado (409 `TURNO_CERRADO`), como en Node; un
  turno que se superpone responde 409 `TURNO_SOLAPADO`; fechas mal formadas, fin anterior al inicio, más de
  3 imágenes o un destinatario que no es tatuador responden 400/404.
- El JWT lleva `roles` además de `id` y `rol`.
- Instagram usa el permiso `instagram_business_basic` (Instagram API con inicio de sesión de Instagram),
  como Node, y el token viaja en el header `Authorization`.
- Errores de Spring MVC (405, 415, 413...) con su código real en lugar de 500.
- Clientes HTTP con `RestClient` (sin WebFlux).
- La clave JWT de desarrollo ya no está en el repositorio: se carga `JWT_SECRET` en `.env`.

### Corregido

- V11 y V1 quedan como están aplicadas en las bases existentes (ADR-0002): la V11 anterior no corría en una
  base nueva.
- Cualquier usuario autenticado podía editar el perfil o la foto de otro.

### Eliminado

- Copias de `ApiResponse`, `PageResponse`, `AppExceptions`, `GlobalExceptionHandler`, `JwtService`,
  `JwtAuthenticationFilter`, `RsaDecryptionService`, `RsaKeyConfig`, `CorsConfig`, `RecaptchaServiceImpl`,
  `JpaAuditingConfig`, entidades y repositorios `Swe*`, `AuthController`, `SweItemmenu*`, y las clases muertas
  `ImagenReferencia`, `SweAuditableEntity`, `SweBaseEntity` y `AppUtils`.
- Tests sobre H2.
- Archivos de Eclipse versionados.

### Seguridad

- Jackson 2.21.7 y 3.1.7 y Tomcat 11.0.27 en lugar de los de Spring Boot 4.1.1, que tienen vulnerabilidades
  altas y críticas (fijadas en el `pom.xml` hasta que el arreglo de dmc00-parent llegue a `develop`).
- V18: el admin semilla queda sin contraseña utilizable (los hashes que sembró V1 eran conocidos y uno es de
  una contraseña publicada en este repositorio) (ADR-0003).
- El `state` del OAuth de Instagram ya no puede usarse como token de sesión.
- V20: las filas que apuntaban a imágenes locales de `/uploads` se retiran y quedan registradas (ADR-0001).
