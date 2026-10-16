# syntax=docker/dockerfile:1.7
#
# Imagen de producción del backend de LINKED (Java 25). Sale de
# dmc00-deploy-templates/templates/Dockerfile.backend-spring.
#
# Se construye con BuildKit desde spring-linked-project/, con las librerías del workspace como
# contextos aparte (en local quedan al lado del repo; en CI se descargan con su versión fija):
#   docker buildx build --build-context parent=../../dmc00-parent --build-context core=../../dmc00-core \
#       -f deploy/Dockerfile -t linked-api .
#
# Variables de entorno: ver .env.example y docs/guia-entorno-local-y-despliegue.md. Sin las obligatorias
# del perfil prod la app no arranca.

# ── Compilación ──────────────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /build
COPY --from=parent pom.xml dmc00-parent/pom.xml
RUN --mount=type=cache,target=/root/.m2 mvn -B -q install -f dmc00-parent/pom.xml
COPY --from=core . dmc00-core
RUN --mount=type=cache,target=/root/.m2 mvn -B -q install -DskipTests -f dmc00-core/pom.xml
COPY pom.xml app/pom.xml
RUN --mount=type=cache,target=/root/.m2 mvn -B -q -f app/pom.xml dependency:go-offline
COPY src app/src
RUN --mount=type=cache,target=/root/.m2 mvn -B -q -f app/pom.xml package -DskipTests \
 && java -Djarmode=tools -jar app/target/spring-linked-project-*.jar extract --layers --launcher --destination capas

# ── Ejecución ────────────────────────────────────────────────────────────────
# tini como PID 1: pasa las señales para un apagado ordenado (blue-green de Fly) y limpia procesos hijos.
FROM eclipse-temurin:25-jre-noble
# apt-get upgrade: los arreglos de seguridad de Ubuntu llegan antes que una imagen base nueva.
RUN apt-get update \
 && apt-get upgrade -y \
 && apt-get install -y --no-install-recommends tini ca-certificates \
 && rm -rf /var/lib/apt/lists/* \
 && groupadd --system --gid 10001 app \
 && useradd --system --uid 10001 --gid app --home-dir /data/home --shell /usr/sbin/nologin app \
 && mkdir -p /data/home \
 && chown -R app:app /data
WORKDIR /app
# El código queda de root y de solo lectura para la aplicación: solo escribe en /data y /tmp.
COPY --from=build /build/capas/dependencies/ ./
COPY --from=build /build/capas/spring-boot-loader/ ./
COPY --from=build /build/capas/snapshot-dependencies/ ./
COPY --from=build /build/capas/application/ ./
ENV TZ=America/Argentina/Buenos_Aires \
    HOME=/data/home \
    SPRING_PROFILES_ACTIVE=prod \
    JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:+ExitOnOutOfMemoryError"
EXPOSE 8080
USER app
ENTRYPOINT ["/usr/bin/tini", "--", "java", "org.springframework.boot.loader.launch.JarLauncher"]
