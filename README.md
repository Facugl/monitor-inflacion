# Monitor de Inflación Argentina

Dashboard que sincroniza el nivel general del IPC desde la [API pública del INDEC](https://api.indec.gob.ar/docs)
hacia una base propia, calcula variación mensual / interanual / acumulada, y simula qué
le pasa al poder de compra de un sueldo nominal fijo mes a mes.

Backend en **Java 17 + Spring Boot 3** con arquitectura hexagonal (dominio sin dependencias
de framework, puertos y adaptadores), frontend en **React + TypeScript**.

## Stack

| Capa | Tecnologías |
|---|---|
| Backend | Java 17 (compilado con JDK 21), Spring Boot 3, Spring Data JPA, PostgreSQL, Flyway, OpenAPI/Swagger |
| Testing | JUnit 5, Mockito, MockMvc, Testcontainers |
| Frontend | React, TypeScript, TanStack Query, Recharts, Tailwind CSS |
| Infra | Docker, Docker Compose, GitHub Actions (CI) |

## Arquitectura (backend)

```
domain/        -> modelo y reglas de negocio, sin Spring ni JPA (framework-free)
application/   -> casos de uso + puertos (interfaces) de entrada/salida
infrastructure/-> adaptadores: REST (web), JPA (persistence), cliente HTTP a la API-INDEC
```

El dominio (`CalculadoraVariacionesIpc`, `CalculadoraPoderCompra`) es Java puro: se puede
testear sin levantar Spring ni una base de datos. La infraestructura es intercambiable
— por ejemplo, cambiar Postgres por otra base implica tocar solo `infrastructure/persistence`.

## 1. Conseguir tu JWT Token de la API-INDEC

1. Entrá a <https://api.indec.gob.ar/docs>
2. Registrate con tu email, nombre y organización
3. Verificá el código OTP que te llega por correo
4. Generá tu JWT Token desde tu perfil — **se muestra una sola vez**, guardalo

## 2. Levantar todo con Docker Compose (recomendado)

```bash
cp .env.example .env
# editá .env y pegá tu INDEC_API_TOKEN

docker compose up --build
```

- Backend: http://localhost:8080 (Swagger UI en `/docs`)
- Frontend: http://localhost:8081

Al abrir el frontend por primera vez no vas a ver datos: hacé click en **"Sincronizar con
INDEC"** (o `POST http://localhost:8080/api/v1/ipc/sync`) para traer la serie histórica.
Después se sincroniza solo todos los días a las 9am (configurable, ver `indec.sync.cron`).

## 3. Correr en modo desarrollo (sin Docker)

**Backend:**
```bash
cd backend
# Levantá un Postgres local, o usá: docker run -p 5432:5432 -e POSTGRES_PASSWORD=monitor_inflacion -e POSTGRES_USER=monitor_inflacion -e POSTGRES_DB=monitor_inflacion postgres:16-alpine
export INDEC_API_TOKEN=tu_token_aca
./mvnw spring-boot:run      # en Windows: .\mvnw.cmd spring-boot:run
```

**Frontend:**
```bash
cd frontend
npm install
cp .env.example .env
npm run dev
```

## Verificar la estructura de datos de la API-INDEC

El endpoint `/ipc_nacional` devuelve, además del índice, variaciones ya calculadas por
el propio INDEC, filtradas por `tipo_de_indicador` y `descripcion` (categoría "Nivel
general"). Los literales exactos de esos campos pueden cambiar; si al sincronizar no
aparecen datos, llamá directamente a la API con tu token para inspeccionar los valores
reales y ajustá `indec.api.nivel-general-descripcion` / `indec.api.tipo-indicador-indice`
en `application.yml` (o las variables de entorno equivalentes) sin tocar código.

Valores verificados contra la API en noviembre de 2026: `descripcion` = `Nivel general`,
`region` = `Nacional` y `tipo_de_indicador` = `Indice` — **sin tilde**. Ojo con este último:
el filtro de PostgREST es sensible a la tilde y con `Índice` devuelve `[]`, lo que hace que
la sincronización reporte "0 puntos" sin ningún error.

```bash
curl -H "Authorization: Bearer $INDEC_API_TOKEN" \
  "https://api.indec.gob.ar/v1/ipc_nacional?region=eq.Nacional&limit=5"
```

## Endpoints principales

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/api/v1/ipc/sync` | Sincroniza el IPC desde la API-INDEC |
| GET | `/api/v1/ipc/evolucion?desde=AAAAMM&hasta=AAAAMM` | Serie con variaciones |
| GET | `/api/v1/ipc/resumen` | Variación del último período cargado |
| POST | `/api/v1/poder-compra` | Simula poder de compra de un sueldo fijo |

Documentación interactiva completa en `/docs` (Swagger UI) una vez levantado el backend.

## Tests

No hace falta tener Maven instalado: el repo incluye el Maven Wrapper (`mvnw`), que
descarga la versión correcta la primera vez.

```bash
cd backend
./mvnw test               # unitarios de dominio (12 tests, sin Docker)
./mvnw verify             # + las *IT de integración con Testcontainers (requiere Docker)
```

En Windows, `.\mvnw.cmd test` / `.\mvnw.cmd verify`.

```bash
cd frontend
npm run build             # type-check + build de producción
```

## Nota sobre este proyecto

Hecho como pieza de portfolio para demostrar el stack de
[Facundo Luna](https://github.com/Facugl) — backend developer (Java/Spring Boot) — usando
datos públicos y de actualidad (la API-INDEC es nueva, en beta al momento de este proyecto).
