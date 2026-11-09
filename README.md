# Monitor de Inflación Argentina

**[Ver el dashboard en vivo](https://monitor-inflacion.vercel.app)** · [API](https://monitor-inflacion-api.onrender.com/docs)

Dashboard que sincroniza el IPC desde la [API pública del INDEC](https://api.indec.gob.ar/docs)
hacia una base propia, calcula variación mensual / interanual / acumulada, muestra qué
rubro aumentó más en el mes, y simula qué le pasa al poder de compra de un sueldo
nominal fijo mes a mes.

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

El dominio (`CalculadoraVariacionesIpc`, `CalculadoraVariacionesPorCategoria`,
`CalculadoraPoderCompra`) es Java puro: se puede testear sin levantar Spring ni una base
de datos. La infraestructura es intercambiable — por ejemplo, cambiar Postgres por otra
base implica tocar solo `infrastructure/persistence`.

La apertura por categoría es el ejemplo de que eso no es decorativo: agregar las doce
divisiones COICOP fue sumar un modelo de dominio, un puerto y sus adaptadores, sin tocar
el cálculo del nivel general. Las dos calculadoras comparten la fórmula
(`VariacionPorcentual`) para que los números del dashboard cierren entre sí.

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

## Despliegue (Render + Neon + Vercel)

Tres piezas, las tres en plan gratuito y sin tarjeta:

| Pieza | Dónde | Por qué ahí |
|---|---|---|
| Postgres | **Neon** | El plan free no expira. El de Render sí, y obliga a migrar a los pocos meses. |
| Backend | **Render** | Corre el contenedor directo desde `render.yaml`. |
| Frontend | **Vercel** | Estático, detecta Vite solo. |

Los archivos ya están versionados: [`render.yaml`](render.yaml) en la raíz (Render
lo exige ahí) y [`frontend/vercel.json`](frontend/vercel.json).

Hay una dependencia circular entre los dominios —el frontend necesita la URL del
backend para compilar, y el backend necesita la del frontend para el CORS—, así
que el orden importa.

### 1. Base en Neon

Creá un proyecto y copiá la cadena de conexión. Vas a necesitar las partes por
separado, porque Spring quiere `jdbc:postgresql://…` y no el `postgres://…` que
te dan hecho:

```
SPRING_DATASOURCE_URL       jdbc:postgresql://TU-HOST.neon.tech/TU-BASE?sslmode=require
SPRING_DATASOURCE_USERNAME  el usuario de Neon
SPRING_DATASOURCE_PASSWORD  la password de Neon
```

`sslmode=require` no es opcional: Neon rechaza las conexiones sin TLS.

### 2. Backend en Render

**New** → **Blueprint** → elegí este repo. Render lee `render.yaml` y pide a mano
las variables marcadas con `sync: false`: las tres de arriba más
`INDEC_API_TOKEN`. `APP_CORS_ALLOWED_ORIGINS` queda para el paso 4.

El plan free **se duerme tras unos minutos de inactividad**, y despertar un Spring
Boot lleva cerca de un minuto. Es el costo de no pagar; ver [Mantenerlo
despierto](#mantenerlo-despierto) más abajo.

Cuando termine, copiá la URL del servicio.

### 3. Frontend en Vercel

Importá el repo con **Root Directory = `frontend`** y definí:

```
VITE_API_BASE_URL    https://TU-BACKEND.onrender.com/api/v1
```

Vite **incrusta las variables en tiempo de build**, no las lee en runtime: si la
cambiás después hay que volver a deployar. Solo se exponen las que empiezan con
`VITE_`.

### 4. Cerrar el círculo

Volvé a Render y agregá, con el dominio que te dio Vercel:

```
APP_CORS_ALLOWED_ORIGINS    https://TU-APP.vercel.app
```

Sin esto el navegador bloquea las llamadas por CORS y el dashboard se ve vacío
aunque la API responda bien.

### Mantenerlo despierto

[`keep-alive.yml`](.github/workflows/keep-alive.yml) pinguea el backend cada 10
minutos para que Render no lo apague.

Le pega a `/actuator/health/liveness`, no a `/actuator/health`. La diferencia
importa: el health completo incluye el indicador de la base, que abre una conexión
y corre una query de validación. Un ping cada 10 minutos contra ese endpoint
mantendría a Neon despierto las 24 horas, y el free de Neon se mide en tiempo de
cómputo activo — el ping terminaría gastando la cuota de la base para sostener la
API. `liveness` responde sin tocar Postgres.

Dos límites que conviene saber: el cron de GitHub Actions es *best effort* y en
horas pico se atrasa, y GitHub desactiva los workflows programados en repos sin
commits por 60 días (avisa por mail antes). Si hace falta puntualidad real, un
cron externo tipo [cron-job.org](https://cron-job.org) contra la misma URL es más
confiable, a cambio de quedar fuera del repo.

### Primera carga de datos

No hay que hacer nada: al arrancar con la base vacía, el backend sincroniza solo
contra la API-INDEC (`indec.sync.al-iniciar`, activo por defecto). Si ya tiene
datos no vuelve a pedirlos. Después sigue el cron diario, y siempre queda el botón
del dashboard o `POST /api/v1/ipc/sync`.

Si falta el `INDEC_API_TOKEN`, la app **arranca igual** y la sincronización falla
con un error en el log: preferible a que el servicio no levante y quede marcado
como caído.

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
| POST | `/api/v1/ipc/sync` | Sincroniza el IPC (nivel general + categorías) desde la API-INDEC |
| GET | `/api/v1/ipc/evolucion?desde=AAAAMM&hasta=AAAAMM` | Serie con variaciones |
| GET | `/api/v1/ipc/resumen` | Variación del último período cargado |
| GET | `/api/v1/ipc/categorias[?periodo=AAAAMM]` | Las 12 divisiones COICOP rankeadas por variación mensual |
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
