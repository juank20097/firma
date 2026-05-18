# ============================================================
#  FirmaIessApi - Dockerfile
#  Autor: Juan Carlos Estévez Hidalgo
#  Fecha: 07 may 2026
#
#  Multi-stage build:
#    Stage 1 (builder) → compila el proyecto con Maven
#    Stage 2 (runtime) → imagen final solo con el JAR
#
#  Uso:
#    Construir:  docker build -t base-spring-api:0.0.1 .
#    Ejecutar:   docker run -p 8080:8080 --env-file .env base-spring-api:0.0.1
# ============================================================

# ── Stage 1: Builder ─────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Copiar solo pom.xml primero para aprovechar cache de capas
# Si el pom no cambia, Maven no re-descarga dependencias
COPY pom.xml .
COPY .mvn/ .mvn/
COPY mvnw .

RUN chmod +x mvnw

# Descargar dependencias (cacheado si pom.xml no cambia)
RUN ./mvnw dependency:go-offline -B

# Copiar el código fuente
COPY src/ src/

# Compilar y empaquetar saltando tests
RUN ./mvnw package -DskipTests -B

# ── Stage 2: Runtime ─────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine AS runtime

# Metadatos de la imagen
LABEL maintainer="Juan Carlos Estévez Hidalgo"
LABEL application="FirmaIessApi"
LABEL version="1.0.0"

# Usuario no root por seguridad
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# Carpeta para almacenamiento local de archivos (cuando MINIO_ENABLED=false)
RUN mkdir -p uploads && chown appuser:appgroup uploads

# Copiar solo el JAR desde el stage builder
COPY --from=builder /app/target/FirmaIessApi-1.0.0.jar app.jar

# Cambiar al usuario no root
USER appuser

# Puerto de la aplicación
EXPOSE 8080

# JVM optimizada para contenedores
ENV JAVA_OPTS="-XX:+UseContainerSupport \
               -XX:MaxRAMPercentage=75.0 \
               -XX:+UseG1GC \
               -Djava.security.egd=file:/dev/./urandom"

# Punto de entrada
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
