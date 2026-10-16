# Documentación técnica — LINKED (backend)

Para: quien programa en el backend. Responde cómo está armado y cómo se cambia sin romperlo. Las reglas
comunes del workspace están en `dmc00-workspace/docs/convenciones/backend-java.md`.

## 1. Arquitectura

API REST en Java 25 y Spring Boot 4.1 (`spring-linked-project/`), padre `dmc00-parent`, con los módulos
de `dmc00-core`:

| Módulo de core | Qué aporta a linked |
|---|---|
| `core-web` | `ApiResponse`, `AppExceptions` y `GlobalExceptionHandler` (todas las respuestas y errores) |
| `core-data` | `BaseEntity` (id, versión, auditoría, baja lógica) y la auditoría JPA |
| `core-security` | JWT, filtro, RSA para las contraseñas, `PasswordEncoder` (BCrypt + Argon2 de Node), CORS, reCAPTCHA, 401/403 en JSON |
| `core-swe` | usuarios, roles, menú, `/api/auth` (login, clave pública, disponibilidad) y sus migraciones |
| `core-test` (tests) | `PostgresContenedor` (Testcontainers) y reglas de ArchUnit |

Capas: `controller` (HTTP y permiso de cada endpoint) → `service` (interfaz) / `service.impl` (reglas y
transacciones) → `repository`. Lo externo va por puertos en `integration` (`InstagramApi`,
`AlmacenDeImagenes`) y las tareas en segundo plano en `job`. Diagramas en [diagramas/](diagramas/).

`nodejs-legacy/` es la versión anterior (Node 20, Express, TypeORM). Sigue en `main` hasta que se retire;
qué quedó de cada endpoint: [paridad-con-node.md](paridad-con-node.md).

## 2. Módulos

| Módulo | Responsabilidad | Clases principales |
|---|---|---|
| Usuarios | registro de clientes y tatuadores, perfil, foto, roles, recuperación de contraseña | `UserController`, `UserServiceImpl`, `PerfilUsuario` |
| Solicitudes | alta como tatuador aprobada por un administrador | `SolicitudTatuadorController`, `SolicitudTatuadorServiceImpl` |
| Especialidades | estilos de tatuaje y su asignación a tatuadores | `EspecialidadesController`, `TatuadorEspecialidadController` |
| Agenda | horario habitual y horarios libres | `AgendaController`, `AgendaServiceImpl` |
| Turnos | pedido, gestión de estado y chat | `TurnosController`, `TurnosServiceImpl`, `EstadoTurno` |
| Trabajos | portfolio y likes | `TrabajosController`, `TrabajosServiceImpl` |
| Búsqueda | tatuadores por mapa (y por estilo, con IA pendiente) | `BusquedaController`, `BusquedaServiceImpl` |
| Instagram | OAuth, importación de publicaciones, sincronización cada 2 h | `InstagramController`, `InstagramServiceImpl`, `SincronizacionDeInstagram`, `InstagramGraphApi` |
| Soporte | tickets | `SupportTicketController`, `SupportTicketServiceImpl` |
| Noticias y legales | home y páginas de privacidad, términos y borrado de datos | `NewsController`, `LegalController` |

### 2.1 Usuarios y perfil

- La cuenta (`swe_usrapl`, credenciales, roles) es de core-swe: el registro llama a
  `SweUsrService.register(..., "Cliente")` y después guarda el perfil de linked en la misma transacción.
- `PerfilUsuario` (`tat_perfil_usuario`) tiene el mismo id que el usuario. Las entidades del dominio
  siguen apuntando al usuario (`SweUsrapl`); cuando una respuesta necesita datos del perfil, el servicio
  busca los perfiles de una vez con `PerfilUsuarioRepository#porUsuario` y se los pasa al mapper. Un usuario
  sin perfil (alta por fuera de la app) se muestra con esos campos vacíos.
- `PUT /api/users/{id}` y `PATCH /api/users/{id}/profile-photo`: el propio usuario o un administrador
  (`Permisos.EL_MISMO_O_ADMINISTRADOR`). Reemplazan al `PUT /api/users/updateUser/:id` de Node.

### 2.2 Seguridad

- `SecurityConfig` aplica lo común de core-security y solo separa rutas públicas de autenticadas. El
  permiso de cada endpoint está en su `@PreAuthorize` con las constantes de `Permisos`; `ArquitecturaTest`
  falla si un endpoint no declara el suyo.
- El JWT lleva `sub` (username), `id`, `roles` y `rol` (el más alto según
  `app.swe.jerarquia-de-roles`). El backend no autoriza con esos claims: recarga el usuario en cada request.
- `JwtConTokenEnInicioDeOAuth` acepta el JWT como parámetro `token` solo en `/api/instagram/auth` (el
  navegador llega por redirección y no puede mandar headers).
- El `state` del OAuth es un JWT con subject `estado-oauth-de-instagram:<id>`: más largo que cualquier
  username (25), así no sirve como token de sesión.

### 2.3 Instagram

- `InstagramGraphApi` habla con la Graph API (`instagram_business_basic`); las llamadas a Instagram y a
  Cloudinary van fuera de las transacciones y cada escritura en la base usa una transacción corta
  (`TransactionTemplate`).
- La sincronización importa imágenes y carruseles (no videos), copiando cada imagen a Cloudinary porque las
  URLs de Instagram vencen, y da de baja (baja lógica) los trabajos cuya publicación se borró.
- `SincronizacionDeInstagram` la dispara cada `app.instagram.sincronizacion.cada` (PT2H), al iniciar
  sesión un tatuador (evento `InicioDeSesion` de core-swe) y al terminar la vinculación.

### 2.4 Migraciones

- `db/migration/linked` con el historial `flyway_schema_history`; `baseline-on-migrate` toma una base que
  viene de Node como V1. core-swe corre después (`DESPUES_DE_LA_APP`) con su historial
  `flyway_historial_swe`.
- V1 y V11 quedaron como estaban aplicadas ([ADR-0002](decisiones/ADR-0002-v1-y-v11-quedan-como-estan-aplicadas.md)).
- V17 alinea dos columnas con las entidades; V18 deja al admin semilla sin contraseña
  ([ADR-0003](decisiones/ADR-0003-admin-semilla-sin-contrasena.md)); V19 crea `tat_perfil_usuario`; V20
  retira las rutas de `/uploads` ([ADR-0001](decisiones/ADR-0001-rutas-locales-de-uploads.md)).
- Pendiente para la versión siguiente: una migración que borre de `swe_usrapl` las columnas que pasaron
  al perfil (`estudio`, `fecha_inicio_actividad`, `localidad`, `lat`, `lng`, `instagram_handle`).

## 3. Cómo agregar un módulo

Checklist de `backend-java.md` §11: migración Flyway nueva (nunca editar una aplicada), entidad que
extiende `BaseEntity`, repositorio, `XxxDto` con records y validaciones, `XxxService` + `XxxServiceImpl`
(`@Transactional(readOnly = true)` en la clase), mapper de MapStruct, controller con `@PreAuthorize` en
cada método, tests (unitario del servicio y un `*IT` del endpoint), DER, clases, paridad si reemplaza algo
de Node, y CHANGELOG.

## 4. Tests

| Tipo | Clases | Necesita |
|---|---|---|
| Unitarios (`*Test`, Surefire) | `ArquitecturaTest`, `InstagramGraphApiTest` (Meta simulado con `MockRestServiceServer`), `JwtConTokenEnInicioDeOAuthTest`, `AdminSemillaDevRunnerTest`, `BusquedaServiceImplTest` | nada |
| Integración (`*IT`, Failsafe) | `AutenticacionIT`, `UsuariosIT`, `TurnosIT`, `InstagramIT` (base `PruebaDeIntegracion`), `MigracionesIT` | Docker (sin Docker se saltean) |

- Los `*IT` levantan la app con el perfil `test` contra PostgreSQL 17 en Testcontainers; Flyway corre las
  migraciones reales. Comparten contexto y base: cada test crea sus usuarios con nombres únicos.
- `DoblesDePrueba` reemplaza Instagram, Cloudinary y el mail: ningún test sale a la red.
- `MigracionesIT` migra hasta una versión, carga datos como estaban y sigue (V11, V18, V19, V20).

```bash
cd spring-linked-project
./mvnw verify        # todo
./mvnw test          # solo unitarios
```

## 5. Definición de terminado

Compila · pasan todos los tests · el código nuevo tiene tests · docs y diagramas actualizados en la
misma rama · CHANGELOG en "Sin publicar" · CI en verde.

## 6. Trampas conocidas

- `BaseEntity` inicializa `version` en 0: Spring Data trata las entidades nuevas como existentes y hace
  `merge`. Usar siempre la instancia que devuelve `save()` antes de agregarla a una colección con cascade.
- Hibernate ejecuta los INSERT antes que los DELETE pendientes: al reemplazar una colección con
  restricción única (horario por día, roles) hace falta `flush()` entre borrar y agregar.
- Las consultas con `JOIN FETCH` traen una sola colección `List` por vez (`MultipleBagFetchException`).
- `ddl-auto: validate`: cada campo JPA tiene que coincidir con la migración; la migración se escribe antes.
- El `.env` vacío pisa los valores por defecto: una variable que no se usa va comentada.
- `@Scheduled` lee `app.instagram.sincronizacion.*` como texto: usar formato ISO-8601 (`PT2H`).
- Un cliente no puede cancelar su propio turno: el cliente web llama a `PATCH /gestionar`, que es del
  tatuador (igual que en Node). Queda como mejora.
