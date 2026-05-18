# FirmaIessApi

API REST base desarrollada con **Spring Boot 3** para el **Instituto Ecuatoriano de Seguridad Social (IESS)**.
Implementa arquitectura hexagonal, auditoría con MongoDB, almacenamiento de archivos con MinIO, gestión de secretos con Vault y soporte para PostgreSQL y Oracle 19c.

---

## Tabla de contenidos

- [Arquitectura](#arquitectura)
- [Tecnologías](#tecnologías)
- [Requisitos previos](#requisitos-previos)
- [Configuración del entorno](#configuración-del-entorno)
- [Ecosistema de utilitarios](#ecosistema-de-utilitarios)
- [Ejecutar en local (Eclipse)](#ejecutar-en-local-eclipse)
- [Ejecutar dockerizado](#ejecutar-dockerizado)
- [Variables de entorno](#variables-de-entorno)
- [Vault — Gestión de secretos](#vault--gestión-de-secretos)
- [Swagger UI](#swagger-ui)
- [Endpoints disponibles](#endpoints-disponibles)
- [Auditoría MongoDB](#auditoría-mongodb)
- [Almacenamiento de archivos](#almacenamiento-de-archivos)
- [Motor de base de datos](#motor-de-base-de-datos)
- [Estructura del proyecto](#estructura-del-proyecto)
- [Agregar nuevos secretos a Vault](#agregar-nuevos-secretos-a-vault)

---

## Arquitectura

El proyecto sigue una **arquitectura hexagonal simplificada**:

```
model/                        → Entidades de dominio (sin dependencias)
application/
  port/                       → Contratos (interfaces) de salida
  usecase/                    → Lógica de negocio
infrastructure/
  controller/                 → Adaptadores de entrada HTTP (REST)
  controller/dto/             → DTOs de request y response
  mapper/                     → Conversión entre capas
  persistence/jpa/            → Entidades JPA y repositorios Spring Data
  persistence/mongo/          → Documentos y repositorios MongoDB (auditoría)
  config/                     → Configuración de beans e infraestructura
```

**Regla fundamental:** las dependencias siempre apuntan hacia adentro.  
`controller` → `usecase` → `model`. La capa `model` no depende de nadie.

---

## Tecnologías

| Tecnología | Versión | Uso |
|---|---|---|
| Java | 21 | Lenguaje |
| Spring Boot | 3.3.5 | Framework principal |
| Spring Data JPA | - | Persistencia relacional |
| Spring Data MongoDB | - | Auditoría |
| Spring Cloud Vault | - | Gestión de secretos |
| Spring AOP | - | Aspecto de auditoría |
| PostgreSQL | 16 | Motor relacional (desarrollo) |
| Oracle | 19c | Motor relacional (institucional) |
| MongoDB | 7 | Auditoría de operaciones |
| HashiCorp Vault | 1.15 | Gestión de secretos |
| MinIO | 2024-11 | Almacenamiento de archivos PDF |
| Lombok | - | Reducción de boilerplate |
| Springdoc OpenAPI | - | Documentación Swagger |
| Docker | - | Contenedorización |
| Maven | 3.x | Gestión de dependencias |

---

## Requisitos previos

- **JDK 21**
- **Maven 3.x**
- **Docker Desktop** con Docker Compose
- **Eclipse** o cualquier IDE compatible con Spring Boot
- **MongoDB Compass** (opcional, para visualizar auditoría)

---

## Configuración del entorno

### 1. Clonar el repositorio

```bash
git clone <url-del-repositorio>
cd FirmaIessApi
```

### 2. Crear el archivo `.env`

```bash
cp .env.example .env
```

Editar `.env` con los valores correspondientes al ambiente.

---

## Ecosistema de utilitarios

Levanta todos los servicios de infraestructura necesarios:

```bash
docker-compose -f docker-compose-utilitarios.yml up -d
```

Esto inicia:
- **PostgreSQL** en el puerto `5432`
- **MongoDB** en el puerto `27017`
- **HashiCorp Vault** en el puerto `8200`
- **vault-init** — carga los secretos en Vault automáticamente
- **MinIO** en los puertos `9000` (API) y `9001` (consola web)
 
```
Verificar y ajustar los puertos configurados en los respectivos archivos Docker Compose para evitar conflictos con puertos que ya se encuentren en uso.
```


### Verificar que los utilitarios están corriendo

```bash
docker-compose -f docker-compose-utilitarios.yml ps
```

### Recargar secretos en Vault

```bash
docker-compose -f docker-compose-utilitarios.yml up vault-init
```

### Detener utilitarios

```bash
docker-compose -f docker-compose-utilitarios.yml down
```

---

## Ejecutar en local (Eclipse)

1. Asegurarse de que los utilitarios estén corriendo
2. Verificar que `.env` tenga `IP_SERVER=` vacío y `VAULT_HOST=localhost`
3. Ejecutar `FirmaIessApiApplication.java` desde Eclipse
4. Verificar el arranque en:

```
http://localhost:8080/api/actuator/health
```

---

## Ejecutar dockerizado

### 1. Construir y levantar la aplicación

```bash
docker-compose up -d --build
```

### 2. Solo levantar (sin reconstruir la imagen)

```bash
docker-compose up -d
```

### 3. Ver logs en tiempo real

```bash
docker-compose logs -f
```

### 4. Detener la aplicación

```bash
docker-compose down
```

### 5. Reconstruir la imagen desde cero

```bash
docker-compose build --no-cache
docker-compose up -d
```

> **Importante:** Cuando la aplicación corre dockerizada, `IP_SERVER` en el `.env`
> debe tener la IP real del servidor donde corren los utilitarios.
> Ejemplo: `IP_SERVER=192.168.12.42`

---

## Variables de entorno

Copiar `.env.example` como `.env` y completar los valores.

| Variable | Descripción | Requerida |
|---|---|---|
| `IP_SERVER` | Vacío=local, IP=dockerizado | No |
| `DB_ENGINE` | Motor de BD: `postgres` u `oracle` | Sí |
| `VAULT_ENABLED` | `true` activa lectura de secretos desde Vault | Sí |
| `VAULT_HOST` | Host del servidor Vault | Sí |
| `VAULT_TOKEN` | Token de autenticación en Vault | Sí |
| `MONGO_ENABLED` | `true` activa auditoría en MongoDB | No |
| `MINIO_ENABLED` | `true` activa almacenamiento en MinIO | No |
| `DB_POSTGRES_DDL_AUTO` | `update` en desarrollo, `none` en producción | Sí |

Las variables marcadas con `[VAULT]` en el `.env` son provistas automáticamente
por Vault cuando `VAULT_ENABLED=true`. En caso contrario deben completarse manualmente.

---

## Vault — Gestión de secretos

### Secretos almacenados

| Path en Vault | Claves | Variable del sistema |
|---|---|---|
| `FirmaIessApi/database/postgres` | `host`, `port`, `username`, `password`, `bdd` | `DB_POSTGRES_*` |
| `FirmaIessApi/database/oracle` | `host`, `port`, `username`, `password`, `service` | `DB_ORACLE_*` |
| `FirmaIessApi/database/mongo` | `host`, `port`, `bdd`, `username`, `password`, `auth_db` | `DB_MONGO_*` |
| `FirmaIessApi/storage/minio` | `url`, `accessKey`, `secretKey` | `MINIO_*` |

### Acceder a la UI de Vault

```
http://localhost:8200
```

- Método: **Token**
- Token: `root-token`

### Consultar un secreto manualmente

```bash
docker exec -e VAULT_TOKEN=root-token -e VAULT_ADDR=http://127.0.0.1:8200 \
  -it base_spring_vault vault kv get FirmaIessApi/database/postgres
```

---

## Swagger UI

Una vez levantada la aplicación, la documentación interactiva está disponible en:

```
http://localhost:8080/api/swagger-ui.html
```

JSON del spec OpenAPI:

```
http://localhost:8080/api/api-docs
```

---

## Endpoints disponibles

### Personas

| Método | Endpoint | Descripción |
|---|---|---|
| `POST` | `/api/personas` | Crear persona |
| `PUT` | `/api/personas/{id}` | Actualizar persona |
| `GET` | `/api/personas` | Listar activos (paginado) |
| `GET` | `/api/personas/{id}` | Buscar por ID |
| `GET` | `/api/personas/cedula/{cedula}` | Buscar por cédula |
| `PATCH` | `/api/personas/{id}/status` | Cambiar status (A/I) |
| `DELETE` | `/api/personas/{id}` | Eliminación lógica (status=E) |

### Documentos

| Método | Endpoint | Descripción |
|---|---|---|
| `POST` | `/api/documentos/persona/{personaId}` | Subir documento PDF |
| `GET` | `/api/documentos/persona/{personaId}` | Listar por persona (paginado) |
| `GET` | `/api/documentos/{id}` | Buscar por ID |
| `DELETE` | `/api/documentos/{id}` | Eliminación lógica (status=E) |

### Paginación

Los endpoints de listado aceptan los siguientes query params:

| Parámetro | Default | Descripción |
|---|---|---|
| `pagina` | `0` | Número de página (base 0) |
| `tamanio` | `10` | Registros por página |
| `ordenarPor` | `apellidos` / `createdAt` | Campo de ordenamiento |
| `direccion` | `asc` / `desc` | Dirección del ordenamiento |

### Status de registros

| Valor | Descripción |
|---|---|
| `A` | Activo |
| `I` | Inactivo |
| `E` | Eliminado lógicamente |

---

## Auditoría MongoDB

Cuando `MONGO_ENABLED=true`, todas las operaciones de escritura se registran
automáticamente en la base de datos `AUDITORIA_IESS` en MongoDB.

### Colecciones

| Colección | Contenido |
|---|---|
| `AUD_GEN.PERSONA` | Auditoría de operaciones sobre personas |
| `AUD_GEN.DOCUMENTO` | Auditoría de operaciones sobre documentos |

### Campos del documento de auditoría

| Campo | Descripción |
|---|---|
| `aud_operation` | Tipo: `I` (insert), `U` (update), `D` (delete lógico) |
| `aud_timestamp` | Fecha y hora exacta de la operación |
| `aud_app_user` | Usuario de la aplicación (JWT cuando se integre seguridad) |
| `aud_ip_address` | IP del cliente (soporta X-Forwarded-For) |
| `aud_session_id` | ID de sesión HTTP |
| `aud_program` | Módulo y método que ejecutó la operación |
| `aud_old_values` | Snapshot del registro antes del cambio (UPDATE/DELETE) |
| `aud_new_values` | Snapshot del registro después del cambio (INSERT/UPDATE) |
| `aud_changed_fields` | Lista de campos que cambiaron (UPDATE) |

### Verificar en MongoDB Compass

```
mongodb://mongo_user:mongo_password@localhost:27017/AUDITORIA_IESS?authSource=admin
```

---

## Almacenamiento de archivos

El sistema soporta dos modos de almacenamiento controlados por `MINIO_ENABLED`:

| Modo | Configuración | Comportamiento |
|---|---|---|
| Local | `MINIO_ENABLED=false` | Archivos guardados en `uploads/` del servidor |
| MinIO | `MINIO_ENABLED=true` | Archivos guardados en el bucket `base-spring-bucket` |

### Consola web de MinIO

```
http://localhost:9001
```

- Usuario: `minioadmin`
- Contraseña: `minioadmin`

> El bucket `base-spring-bucket` se crea automáticamente al arrancar si no existe.

---

## Motor de base de datos

El motor activo se controla con la variable `DB_ENGINE` en el `.env`:

```
DB_ENGINE=postgres   # Activa perfil postgres
DB_ENGINE=oracle     # Activa perfil oracle
```

- **PostgreSQL** — disponible en el ecosistema de utilitarios Docker
- **Oracle 19c** — motor institucional externo, configurar host en Vault

---

## Estructura del proyecto

```
FirmaIessApi/
├── src/main/java/iess/gen/FirmaIessApi/
│   ├── model/                          # Entidades de dominio
│   ├── application/
│   │   ├── port/                       # Interfaces de contrato (StoragePort)
│   │   └── usecase/                    # Lógica de negocio
│   └── infrastructure/
│       ├── config/                     # Configuración Spring
│       ├── controller/                 # Controllers REST
│       │   └── dto/                    # DTOs request/response
│       ├── mapper/                     # Mappers entre capas
│       └── persistence/
│           ├── jpa/                    # Entidades JPA y repositorios
│           └── mongo/                  # Documentos y repositorios MongoDB
├── src/main/resources/
│   ├── application.yaml                # Configuración principal
│   ├── bootstrap.yaml                  # Configuración Vault bootstrap
│   └── META-INF/spring.factories       # Registro VaultEnvironmentPostProcessor
├── .env.example                        # Plantilla de variables de entorno
├── vault-init.sh                       # Script de carga de secretos en Vault
├── docker-compose.yml                  # Despliegue de la aplicación
├── docker-compose-utilitarios.yml      # Ecosistema de servicios
└── Dockerfile                          # Imagen de la aplicación
```

---

## Agregar nuevos secretos a Vault

### 1. Agregar el secreto en `vault-init.sh`

```bash
vault kv put FirmaIessApi/nuevo-servicio \
  clave=valor
```

### 2. Registrar la lectura en `VaultEnvironmentPostProcessor.java`

```java
leerSecreto(vaultTemplate, "FirmaIessApi/data/nuevo-servicio",
        Map.of("clave", "NOMBRE_VARIABLE_SISTEMA"), secretos);
```

### 3. Agregar el fallback en `.env.example` y `.env`

```
NOMBRE_VARIABLE_SISTEMA=valor_default
```

### 4. Recargar Vault

```bash
docker-compose -f docker-compose-utilitarios.yml up vault-init
```

---

## Autor

**Juan Carlos Estévez Hidalgo**  
Instituto Ecuatoriano de Seguridad Social — IESS  
© 2026 — Todos los derechos reservados
