# Guía de entorno local y despliegue — LINKED (backend)

Para: quien instala el backend en una PC nueva o publica una versión.

## 1. Entorno local

| Herramienta | Versión | Para qué |
|---|---|---|
| JDK | 25 | compilar y correr el backend (`JAVA_HOME`; el wrapper `mvnw` trae Maven) |
| PostgreSQL | 17 | base local |
| Docker | — | tests de integración (Testcontainers) |
| MailHog o Mailpit | — | opcional: ver los mails en `localhost:1025` |

Pasos:

```bash
# 1. Librerías del workspace (hasta que se publiquen en GitHub Packages)
cd ../dmc00-parent && ./mvnw install
cd ../dmc00-core && ./mvnw install -DskipTests

# 2. Configuración local
cd ../linked-server-main/spring-linked-project
cp .env.example .env          # completar DATABASE_PASSWORD y JWT_SECRET (openssl rand -base64 48)

# 3. Correr
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

La primera vez se genera `rsa-privada.pem` (la clave RSA de desarrollo, ignorada por git). Swagger queda
en `http://localhost:8080/swagger-ui.html`; en dev acepta contraseñas sin cifrar.

### Admin inicial

V1 siembra un usuario `admin` y V18 lo deja sin contraseña utilizable (ADR-0003): ningún login entra con
él hasta que alguien le asigne una.

- **Dev**: definir `ADMIN_DEV_PASSWORD` en `.env`. `AdminSemillaDevRunner` (solo perfil `dev`) se la asigna
  al arrancar si la cuenta sigue sin contraseña.
- **Primer admin en producción**, una de dos (con acceso a la base):
  1. Con el mail configurado (`MAIL_*`): poner un email real al admin semilla y pedir "olvidé mi contraseña"
     desde el cliente web; el enlace llega a ese email.
     ```sql
     UPDATE swe_usrapl SET email = 'admin@dominio-real' WHERE username = 'admin';
     ```
  2. Sin mail: registrarse desde el cliente web con un usuario propio y darle el rol de administrador.
     ```sql
     INSERT INTO swe_usrrolapl (id_usrapl, id_rolapl)
     SELECT u.id, r.id FROM swe_usrapl u, swe_rolapl r
     WHERE u.username = '<usuario>' AND r.description = 'Administrador';
     ```
  En ningún caso se carga un hash a mano en una migración: el repositorio es público.
- **Bases existentes**: V18 neutraliza los dos hashes que sembraron las versiones de V1. Si en alguna base el
  admin semilla tiene la contraseña publicada en el historial del repo cargada por otro medio (otro hash de la
  misma contraseña), V18 no lo detecta: cambiarla con "olvidé mi contraseña" o desactivar la cuenta
  (`DELETE /api/users/{id}` como administrador). Está en los pendientes del dueño del plan del workspace.

## 2. Variables de entorno

La lista completa, con para qué es cada una, está en `spring-linked-project/.env.example`.

| Variable | Obligatoria | Ejemplo | Para qué |
|---|---|---|---|
| `DATABASE_URL` | prod | `jdbc:postgresql://host:5432/linked` | base (en dev, `linked-main-db` local) |
| `DATABASE_USER` / `DATABASE_PASSWORD` | sí | — | credenciales de la base |
| `JWT_SECRET` | sí | salida de `openssl rand -base64 48` | firma de los JWT |
| `RSA_PRIVATE_KEY` | prod | PEM de `openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048` | el cliente cifra las contraseñas con su pública; tiene que ser la misma en todas las máquinas |
| `CORS_ORIGINS` | prod | `https://linked.example` | orígenes del cliente web |
| `FRONT_BASE_URL` | prod | `https://linked.example` | enlaces de los mails y vuelta del OAuth de Instagram |
| `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`, `MAIL_FROM` | prod | — | mails de recuperación de contraseña |
| `RECAPTCHA_SECRET_KEY` | prod | — | reCAPTCHA en login y registro |
| `CLOUDINARY_CLOUD_NAME`, `CLOUDINARY_API_KEY`, `CLOUDINARY_API_SECRET` | prod | — | imágenes |
| `META_APP_ID`, `META_APP_SECRET`, `META_REDIRECT_URI` | para Instagram | `https://api.linked.example/api/instagram/callback` | app de Meta (Instagram API con inicio de sesión de Instagram) |
| `INSTAGRAM_SYNC_INTERVAL` | no | `PT2H` | frecuencia de la sincronización |
| `NEWS_API_KEY` | no | — | noticias de NewsAPI (sin clave, Reddit) |
| `ADMIN_DEV_PASSWORD` | no (solo dev) | — | contraseña local del admin semilla |

## 3. Publicar una versión

Ver `dmc00-workspace/docs/convenciones/git-y-versionado.md` §6. Particularidades:

- `main` todavía es la versión Node. La versión Spring se publica cuando el dueño lleve `develop` a `main`,
  después de pasar la base de producción (pasos en [paridad-con-node.md](paridad-con-node.md), "Retiro").
- El CI (`.github/workflows/ci.yml`) y los controles de seguridad (`seguridad.yml`) descargan
  `dmc00-parent` y `dmc00-core` de sus repos: mientras sean privados hace falta el secret
  `CROSS_REPO_PAT`. Hoy compilan contra `develop` de cada uno; pasar a un tag cuando exista.

## 4. Despliegue

Imagen: `spring-linked-project/deploy/Dockerfile` (multi-etapa, `eclipse-temurin:25-jre`, usuario no
root, `tini`). Se construye desde `spring-linked-project/` con las librerías como contextos:

```bash
docker buildx build --build-context parent=../../dmc00-parent --build-context core=../../dmc00-core \
    -f deploy/Dockerfile -t linked-api .
```

Destino previsto: Fly.io con PostgreSQL administrado (playbook y `fly.backend-spring.toml` en
`dmc00-deploy-templates`). Falta crear la app, cargar los secretos de la tabla y agregar `cd.yml`.

Al arrancar, Flyway migra la base: primero las migraciones de linked y después las de core-swe. Una base
que viene de Node se toma como V1 (`baseline-on-migrate`); hacer un respaldo antes.

## 5. Rollback

- Volver a la imagen anterior. Las migraciones V17–V20 y la V1 de core-swe son compatibles con la versión
  anterior de Spring (agregan tablas y columnas; las columnas viejas de `swe_usrapl` siguen).
- Las contraseñas cambiadas con esta versión quedan como `{bcrypt}...` y la versión anterior no las valida:
  esos usuarios usan "olvidé mi contraseña" desde la versión anterior.
- V20 borra las filas de imágenes locales de `/uploads`; están registradas en `tat_ruta_local_retirada`.
