# Paridad con el backend Node

Para: quien mantiene el cliente web y quien pasa la base de producción de Node a Spring. Responde, endpoint
por endpoint, dónde quedó cada funcionalidad del backend Node en Spring y qué cambió.

El backend Node (`nodejs-legacy/`) se retiró del repositorio el 2026-10-16, con esta tabla verificada
contra su código (cada método y ruta de `src/controllers` tiene su fila). El código sigue en el historial de
git y en `main` hasta que se publique la versión Spring.

Relevado el 2026-10-15 sobre `nodejs-legacy/src/controllers` (68 endpoints) y `spring-linked-project`.
Cada fila está cubierta por la versión Spring; la columna **Cambio** dice si el contrato o el permiso es
distinto. Los permisos de Spring son los de cada `@PreAuthorize` (`com.linked.security.Permisos`).

Cambios generales que el cliente ya absorbió (linked-client-main, rama de trabajo del 2026-10-15):

- Toda respuesta va envuelta en `ApiResponse` (`{success, message, data, errors, codigo, timestamp}`),
  salvo `GET /api/auth/public-key` y las páginas legales (HTML).
- `/findall` y `/create` pasaron a `GET` y `POST` sobre la raíz del recurso.
- Errores con su código HTTP real; los de negocio llevan `codigo` (p. ej. `TURNO_CERRADO`).
- Permisos: Node dejaba varios endpoints de escritura sin control de rol (marcados con ⚠). En Spring
  cada uno exige el rol que corresponde.

## Agenda (`/api/agenda`)

| # | Node | Spring | Permiso Spring | Cambio |
|---|---|---|---|---|
| 1 | `GET /:tatuadorId/fechas-bloqueadas` | `GET /api/agenda/{tatuadorId}/fechas-bloqueadas?desde&hasta` | autenticado | — |
| 2 | `GET /:tatuadorId/slots-dia` | `GET /api/agenda/{tatuadorId}/slots-dia?fecha` | autenticado | — |
| 3 | `GET /horario-habitual` | igual | Tatuador | — |
| 4 | `PUT /horario-habitual` | igual | Tatuador | — |

## Turnos (`/api/turnos`)

| # | Node | Spring | Permiso Spring | Cambio |
|---|---|---|---|---|
| 5 | `POST /solicitar` (multipart, hasta 3 imágenes) | igual | Cliente | además valida fechas ISO, fin posterior al inicio y que el destinatario sea tatuador |
| 6 | `GET /mis-reservas-cliente` | igual | autenticado | — |
| 7 | `GET /mis-turnos` | igual | Tatuador | — |
| 8 | `PATCH /gestionar/:idTurno` | `PATCH /api/turnos/gestionar/{id}` | Tatuador asignado | un turno Completada o Cancelada no cambia (409 `TURNO_CERRADO`), como en Node |
| 9 | `PUT /:id` ⚠ sin autenticación | `PUT /api/turnos/{id}` | participante del turno; el estado, solo el tatuador | exige sesión y participación |
| 10 | `GET /:id/mensajes` | igual | participante del turno | — |
| 11 | `POST /:id/mensajes` | igual | participante del turno | — |

## Autenticación (`/api/auth`, core-swe)

| # | Node | Spring | Permiso Spring | Cambio |
|---|---|---|---|---|
| 12 | `GET /public-key` | igual | público | lo sirve core-swe |
| 13 | `GET /check-availability` | igual | público | lo sirve core-swe |
| 14 | `POST /login` | igual | público | lo sirve core-swe; los hashes Argon2 de Node siguen valiendo (`AutenticacionIT`); al entrar un tatuador se sincroniza Instagram en segundo plano |

## Especialidades (`/api/especialidades`)

| # | Node | Spring | Permiso Spring | Cambio |
|---|---|---|---|---|
| 15 | `GET /findall` | `GET /api/especialidades` | público | ruta |
| 16 | `GET /:id` | igual | público | — |
| 17 | `POST /create` ⚠ cualquier autenticado | `POST /api/especialidades` | Administrador | ruta y permiso |
| 18 | `PUT /:id` ⚠ cualquier autenticado | igual | Administrador | permiso |
| 19 | `DELETE /:id` ⚠ cualquier autenticado | igual | Administrador | permiso; baja lógica |

## Instagram (`/api/instagram`)

| # | Node | Spring | Permiso Spring | Cambio |
|---|---|---|---|---|
| 20 | `GET /auth?tatuadorId&token` | `GET /api/instagram/auth?token` | Tatuador | el tatuador sale del token (`tatuadorId` se ignora); el `state` va firmado y vence en 10 min |
| 21 | `GET /callback` | igual | público | vuelve a `/info?instagram=success\|denied\|error` |
| 22 | `POST /sync` | igual | Tatuador | responde `{nuevos, eliminados, omitidos}`; lo borrado en Instagram se da de baja (Node lo borraba); token vencido: desvincula y 409 `INSTAGRAM_TOKEN_VENCIDO` |
| 23 | `GET /status` | igual | Tatuador | — |
| 24 | `DELETE /disconnect` | igual | Tatuador | limpia usuario y foto de Instagram del perfil, como Node |
| — | scheduler cada 2 h (renueva, desvincula vencidos, sincroniza) | `SincronizacionDeInstagram` | — | mismo intervalo (`INSTAGRAM_SYNC_INTERVAL`); renueva con menos de 30 días; en dev no desvincula |

## Páginas legales y noticias

| # | Node | Spring | Permiso Spring | Cambio |
|---|---|---|---|---|
| 25 | `GET /api/legal/privacidad` | igual | público | — |
| 26 | `GET /api/legal/terminos` | igual | público | — |
| 27 | `GET /api/legal/datadeletion` | igual | público | — |
| 28 | `GET /api/news` | igual | público | NewsAPI y, sin clave o si falla, Reddit r/tattoos |

## Solicitudes para ser tatuador (`/api/solicitudes-tatuador`)

| # | Node | Spring | Permiso Spring | Cambio |
|---|---|---|---|---|
| 29 | `GET /` | igual | Administrador | — |
| 30 | `PUT /:id/aprobar` | igual | Administrador | Cliente → Tatuador y asigna las especialidades elegidas |
| 31 | `PUT /:id/rechazar` | igual | Administrador | — |

## Soporte (`/api/supportTicket`)

| # | Node | Spring | Permiso Spring | Cambio |
|---|---|---|---|---|
| 32 | `GET /me/tickets` | igual | autenticado | — |
| 33 | `GET /findall` | igual | Administrador o Moderador | — |
| 34 | `GET /:id` | igual | Administrador o Moderador | — |
| 35 | `POST /create` | igual | público | — |
| 36 | `PUT /:id` | igual | Administrador o Moderador | — |
| 37 | `POST /:id/respond` | igual | Administrador o Moderador | — |
| 38 | `DELETE /:id` | igual | Administrador o Moderador | — |

## Menú (`/api/sweItemMenu` → `/api/swe/menu`, core-swe)

| # | Node | Spring | Permiso Spring | Cambio |
|---|---|---|---|---|
| 39 | `GET /findall` | `GET /api/swe/menu` | autenticado | solo los ítems visibles para los roles del usuario (lista plana); el administrador tiene además `GET /api/swe/menu/todos` |
| 40 | `GET /:id` | `GET /api/swe/menu/{id}` | Administrador | ruta y permiso |
| 41 | `POST /create` ⚠ cualquier autenticado | `POST /api/swe/menu` | Administrador | ruta y permiso |
| 42 | `PUT /:id` ⚠ cualquier autenticado | `PUT /api/swe/menu/{id}` | Administrador | ruta y permiso |
| 43 | `DELETE /:id` ⚠ cualquier autenticado | `DELETE /api/swe/menu/{id}` | Administrador | ruta, permiso y baja lógica |

## Trabajos (`/api/trabajos`)

| # | Node | Spring | Permiso Spring | Cambio |
|---|---|---|---|---|
| 44 | `GET /feed` | igual | público | — |
| 45 | `POST /` (multipart `imagen`, hasta 5) | `POST /api/trabajos` (multipart `fotos`, hasta 5) | Tatuador | nombre del campo |
| 46 | `GET /tatuador/:id` | igual | público | — |
| 47 | `POST /:id/favorito` | igual | Cliente | — |
| 48 | `DELETE /:id/favorito` | igual | Cliente | — |
| 49 | `GET /mis-favoritos/ids` | igual | autenticado | — |
| 50 | `GET /:id` | igual | autenticado | — |

## Especialidades de cada tatuador (`/api/tatuador`)

| # | Node | Spring | Permiso Spring | Cambio |
|---|---|---|---|---|
| 51 | `GET /:userId/especialidades` | igual | autenticado | antes público; el portfolio público usa `GET /api/users/{id}/publico` |
| 52 | `POST /:userId/asignar-especialidades` ⚠ sin autenticación | igual | Administrador | permiso |
| 53 | `DELETE /:userId/quitar-especialidad/:especialidadId` ⚠ sin autenticación | igual | Administrador | permiso |

## Usuarios (`/api/users`)

| # | Node | Spring | Permiso Spring | Cambio |
|---|---|---|---|---|
| 54 | `GET /findall` ⚠ público | `GET /api/users` | Administrador | ruta y permiso |
| 55 | `GET /getAllRoles` | igual | Administrador | — |
| 56 | `GET /tatuadores` ⚠ público | igual | autenticado | permiso |
| 57 | `GET /:id` ⚠ público | igual | autenticado | permiso; sin sesión: `GET /api/users/{id}/publico` |
| 58 | `POST /register` | igual | público | verifica reCAPTCHA (en prod); crea el perfil de linked |
| 59 | `POST /register-tatuador` | igual | público | ídem; queda Cliente con una solicitud pendiente |
| 60 | `PUT /:id` (multipart con `image`) | `PUT /api/users/{id}` (JSON) | el mismo usuario o Administrador | la foto va por el endpoint 61; antes cualquier usuario podía editar a otro |
| 61 | `PATCH /:id/profile-photo` (multipart `image`) | igual (multipart `file`) | el mismo usuario o Administrador | 403 `FOTO_SINCRONIZADA_CON_INSTAGRAM` si tiene Instagram vinculado, como Node |
| 62 | `POST /forgot-password` | igual | público | no revela si el email existe |
| 63 | `POST /reset-password` | igual | público | — |
| 64 | `DELETE /:id` ⚠ cualquier autenticado | igual | Administrador | permiso; baja lógica |
| 65 | `PUT /updateUser/:id` ⚠ cualquier autenticado | `PUT /api/users/{id}` | Administrador (o el mismo usuario) | mismo endpoint que el 60: el administrador edita nombre, apellido, fecha de nacimiento, estudio, inicio de actividad, ubicación e Instagram de cualquiera. Username, email y estado no se editan: el username es el identificador del login y del JWT; la baja es `DELETE` |
| 66 | `GET /getUserRolsByIdUser/:idUser` ⚠ público | igual | autenticado | permiso |
| 67 | `PUT /:id/roles` | igual | Administrador | — |
| 68 | `GET /getUserRolByidRole/:idRole` ⚠ público | igual | autenticado | permiso |

## Lo que no es un endpoint

| Node | Spring | Dónde |
|---|---|---|
| Archivos locales en `/uploads` (`express.static`) | No se sirven; las filas que apuntaban ahí se retiran (V20) | [ADR-0001](decisiones/ADR-0001-rutas-locales-de-uploads.md) |
| Hashes Argon2id de los usuarios creados en Node | Los valida `CodificadorDeContrasenas` de core-security | `AutenticacionIT#unUsuarioTraidoDeNodeConHashArgon2IniciaSesion` |
| Columnas `nombre`, `apellido`, `nombre_usuario`... de `swe_usrapl` | V11 las copia si existen | `MigracionesIT#v11CopiaLasColumnasDeNodeSiExisten` |

## Solo en Spring

`GET /api/users/{id}/publico` (perfil público del tatuador), `GET /api/busqueda/tatuadores-ubicacion`,
`POST /api/busqueda/por-estilo` (detección de estilo con IA pendiente) y `GET /api/swe/menu/todos`.

## Retiro

Hecho (2026-10-16): la carpeta `nodejs-legacy/` y su configuración (`.renderignore`, `.nvmrc`, la
configuración de depuración de Node en `.vscode/launch.json`) salieron del repositorio en un PR propio. La
carpeta queda ignorada por git para que una copia local (con `node_modules` o imágenes de `uploads`) no se
vuelva a versionar.

Falta, a cargo del dueño:

1. Con la base de producción de Node: hacer un respaldo, apuntar la versión Spring a esa base (Flyway la
   toma como V1 por `baseline-on-migrate`) y revisar `tat_ruta_local_retirada` después de la V20.
2. Probar el cliente web contra Spring (login de un usuario de Node, reservas, Instagram).
3. Llevar `develop` a `main` y apagar el servicio donde corra la versión Node, si hay uno.
