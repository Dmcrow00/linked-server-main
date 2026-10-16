# linked-server-main — Guía de desarrollo

Backend de LINKED (estudios de tatuajes). Reglas generales en `dmc00-workspace/CLAUDE.md` y
`dmc00-workspace/docs/convenciones/backend-java.md`; acá solo lo propio de este repo.

## Estructura

```
spring-linked-project/   backend: Java 25, Spring Boot 4.1, dmc00-parent + dmc00-core
nodejs-legacy/           versión Node anterior: NO se modifica; se retira (docs/paridad-con-node.md)
docs/                    técnica, funcional, entorno, paridad con Node, diagramas, ADR
```

## Comandos

```bash
cd spring-linked-project
./mvnw verify                                           # todo (los *IT necesitan Docker)
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev   # necesita .env (copiar .env.example)
```

`JAVA_HOME` = JDK 25; `dmc00-parent` y `dmc00-core` instalados en `~/.m2`.

## Reglas

- El repo es **público**: ningún secreto ni dato personal en código, yml, tests ni commits. Los valores
  van en `.env` (ignorado) o en los secretos del entorno.
- Usuarios, login, roles, menú, JWT, errores y `ApiResponse` son de `dmc00-core`: no se copian acá. Lo
  propio de linked de un usuario va en `PerfilUsuario` (`tat_perfil_usuario`), no en `swe_usrapl`.
- Todo endpoint lleva `@PreAuthorize` con una constante de `security/Permisos` (lo verifica ArchUnit).
- Instagram y Cloudinary se usan por sus puertos (`InstagramApi`, `AlmacenDeImagenes`); los tests usan los
  dobles de `soporte/DoblesDePrueba`. Ningún test sale a la red.
- Migraciones: nunca editar una aplicada (ver ADR-0002 por qué V1 y V11 quedaron como están); carpeta
  `db/migration/linked`.
- Un cambio en un endpoint actualiza `docs/paridad-con-node.md` mientras exista `nodejs-legacy/`.

## Reglas git

Claude NO ejecuta comandos git de escritura sin instrucción explícita del usuario en ese turno (regla de
este repo, más restrictiva que ADR-0004 del workspace; el dueño decide si pasa a la política común).
Con instrucción: ramas `r.ceballos/<TIPO>_<camelCase>` desde `develop`, PR contra `develop`, nunca merge ni
tags. `main` sigue siendo la versión Node.
