# linked-server-main

Backend de **LINKED**, la plataforma que conecta clientes con estudios de tatuajes: portfolio (propio o
importado de Instagram), búsqueda por zona, agenda, turnos con chat y soporte. Lo consume el cliente web
`linked-client-main` (Angular).

El backend está en `spring-linked-project/` (Java 25, Spring Boot 4.1, PostgreSQL, Flyway, `dmc00-core`).
La versión anterior en Node (`nodejs-legacy/`) se retiró: sigue en el historial de git y en `main` hasta
que se publique esta versión. Qué quedó de cada endpoint: [docs/paridad-con-node.md](docs/paridad-con-node.md).

## Cómo correrlo

```bash
cd spring-linked-project
cp .env.example .env                                   # completar DATABASE_PASSWORD y JWT_SECRET
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
./mvnw verify                                          # tests (los de integración necesitan Docker)
```

Requisitos: JDK 25, PostgreSQL 17, Docker para los tests, y `dmc00-parent` y `dmc00-core` instalados en
`~/.m2` (ver [docs/guia-entorno-local-y-despliegue.md](docs/guia-entorno-local-y-despliegue.md)).

## Documentación

[docs/README.md](docs/README.md) · [CHANGELOG.md](CHANGELOG.md)
