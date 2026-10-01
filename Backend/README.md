# 🖥️ Mimun — Backend

API REST de Mimun, hecha con **Python + FastAPI + PostgreSQL**, siguiendo **Clean Architecture estricta**.

> Antes de trabajar acá, leer el [README raíz](../README.md). Este documento detalla solo el backend. Si algo contradice al README raíz, gana el raíz.
> El plan de trabajo está en [ROADMAP.md](./ROADMAP.md). El contrato de la API vive en [`../Docs/api/`](../Docs/api/). Las decisiones en [`../Docs/decisions/`](../Docs/decisions/README.md).

---

## 📍 Estado actual

🟡 **Fases B0 a B4 y B6 terminadas.** Funciona el CRUD de `mood-entries` y `journal-entries` y la autenticación (`/auth/register`, `/login`, `/refresh`, `/logout`): 93 tests en verde, migraciones `0001` y `0002` aplicadas.

Con `AUTH_ENABLED=true` los endpoints de datos exigen `Authorization: Bearer <access_token>`. Con `AUTH_ENABLED=false` la API usa un usuario fijo de desarrollo, sin token (así se trabaja la Fase F6 del Frontend). En producción `AUTH_ENABLED=true` es obligatorio y la app se niega a arrancar si no.

**Pendiente:** sincronización (B7, la siguiente), estadísticas (B5, opcional), calidad y endurecimiento (B8) y deploy (B9). Falta validar los endpoints desde la app Android (F6).

> Actualizar esta sección cuando cambie el estado real.

---

## 🎯 Responsabilidades

- Usuarios y autenticación.
- Registros emocionales (`MoodEntry`).
- Reflexiones (`JournalEntry`).
- Historial y estadísticas.
- Persistencia en PostgreSQL.
- Exponer una API REST consumida por la app Android.

**Fuera de alcance:** cualquier lógica de UI, animaciones o presentación.

---

## 🧰 Stack

| Pieza | Tecnología |
|---|---|
| Lenguaje | Python 3.12+ |
| Framework | FastAPI |
| Servidor | Uvicorn |
| Base de datos | PostgreSQL |
| ORM | SQLAlchemy 2.x |
| Migraciones | Alembic |
| Validación de la API | Pydantic v2 (solo en `presentation`) |
| Configuración | pydantic-settings (`.env`) |
| Auth | JWT + hash de contraseñas (argon2 o bcrypt) |
| Tests | pytest + httpx |
| Lint y formato | ruff |
| Verificación de capas | import-linter |
| Contenedores | Docker Compose (PostgreSQL en desarrollo) |

---

## 🧱 Clean Architecture

La regla de dependencia apunta **siempre hacia adentro**: el código de una capa solo conoce a las capas más internas.

```
presentation ──▶ application ──▶ domain ◀── infrastructure
   (HTTP)         (casos de uso)   (negocio)    (DB, JWT, hashing)
```

Es el mismo criterio que usa Android (`presentation → domain ← data`).

| Capa | Contiene | Puede importar | No puede importar |
|---|---|---|---|
| **domain** | Entidades, `MoodType`, interfaces de repositorio, errores de dominio. | Solo la librería estándar de Python. | FastAPI, Pydantic, SQLAlchemy, `application`, `infrastructure`, `presentation`. |
| **application** | Casos de uso, *ports* (`PasswordHasher`, `TokenProvider`, `Clock`), commands y results. | `domain`. | FastAPI, Pydantic, SQLAlchemy, `infrastructure`, `presentation`. |
| **infrastructure** | Modelos SQLAlchemy, implementaciones de repositorio, mappers, argon2/JWT, config. | `domain`, `application` (los ports que implementa). | `presentation`. |
| **presentation** | Routers, schemas Pydantic, manejo de errores HTTP, dependencias de FastAPI. | `application`, `domain`. | `infrastructure` (salvo a través del composition root). |

`app/container.py` es el **composition root**: el único lugar donde se conectan las implementaciones concretas con los casos de uso.

Reglas de la arquitectura:

- **Un caso de uso = una clase con un método `execute`** y una sola responsabilidad (`CreateMoodEntry`, `ListMoodEntries`…).
- Las **entidades de dominio** son dataclasses puras. No son modelos SQLAlchemy ni schemas Pydantic: se convierten con mappers en `infrastructure` y en `presentation`.
- Las **interfaces de repositorio** (`Protocol`) viven en `domain`. Sus implementaciones, en `infrastructure`.
- Toda operación de repositorio de datos recibe `user_id`: cada usuario solo accede a **sus propios datos**.
- Cada operación de escritura de un repositorio es una transacción (`commit` por operación), y allí mismo se traducen los errores de integridad a errores de dominio.
- Las **reglas de negocio** (ej.: un registro por día) viven en `domain` y `application`, nunca en routers ni en repositorios.
- Los **errores de dominio** (`MoodEntryAlreadyExistsError`, `NotFoundError`…) se traducen a HTTP **solo** en `presentation`.
- Los tests de `application` usan **repositorios fake en memoria**, sin base de datos.
- `import-linter` verifica en CI que ninguna capa rompa la regla de dependencia.

---

## 🗂️ Estructura

```
Backend/
├── app/
│   ├── domain/
│   │   ├── entities/              # User, MoodEntry, JournalEntry, RefreshToken
│   │   ├── value_objects/         # MoodType
│   │   ├── repositories/          # Interfaces (Protocol)
│   │   └── errors.py              # Errores de dominio
│   ├── application/
│   │   ├── ports/                 # Clock, PasswordHasher, TokenService
│   │   └── use_cases/             # Un módulo por recurso
│   │       ├── mood_entries.py
│   │       ├── journal_entries.py
│   │       ├── auth.py
│   │       └── stats.py           # B5 (opcional)
│   ├── infrastructure/
│   │   ├── config.py              # Settings desde variables de entorno
│   │   ├── db/
│   │   │   ├── base.py            # Base declarativa
│   │   │   ├── session.py         # Engine y sesión
│   │   │   └── models/            # Modelos SQLAlchemy
│   │   ├── repositories/          # Implementaciones SQLAlchemy
│   │   └── security/              # Hash de contraseñas, JWT
│   ├── presentation/
│   │   └── api/
│   │       ├── deps.py            # Dependencias de FastAPI
│   │       ├── error_handlers.py  # Errores de dominio → HTTP
│   │       ├── schemas/           # Schemas Pydantic (request / response)
│   │       └── v1/
│   │           ├── auth.py
│   │           ├── mood_entries.py
│   │           ├── journal_entries.py
│   │           ├── stats.py
│   │           └── health.py
│   ├── container.py               # Composition root
│   └── main.py
├── alembic/                       # Migraciones
├── tests/
│   ├── unit/                      # domain y application (repos fake)
│   ├── integration/               # infrastructure con PostgreSQL real
│   ├── api/                       # endpoints con httpx
│   └── conftest.py
├── .env.example
├── docker-compose.yml
├── alembic.ini
├── pyproject.toml                 # ruff, pytest, import-linter
├── requirements.txt
├── ROADMAP.md
└── README.md
```

---

## 🗄️ Modelo de datos

El esquema detallado (columnas, restricciones e índices) vive en [`../Docs/database/schema.md`](../Docs/database/schema.md). Resumen:

```
users 1 ─── N mood_entries
users 1 ─── N journal_entries
mood_entries 1 ─── 0..N journal_entries
```

- `mood_entries`: `UNIQUE (user_id, date)`, es decir, un registro por usuario y día.
- `journal_entries.mood_entry_id` es opcional (`ON DELETE SET NULL`).
- Todo cambio de esquema requiere **migración de Alembic** y actualizar `Docs/database/`.

---

## 😊 Estados emocionales

Los valores son **exactamente** estos, en mayúsculas y en inglés, idénticos en todas las capas y en Android (`MoodType`):

`RAD`, `GOOD`, `MEH`, `BAD`, `AWFUL`

Escala de mejor a peor: `RAD → GOOD → MEH → BAD → AWFUL`.

> `NORMAL` fue **eliminado por decisión del autor**. Si aparece en algún documento, está desactualizado.

Se definen una sola vez en un `Enum` de Python dentro de `domain/value_objects/` y se reutilizan en todas las capas y tests. Para las estadísticas, el dominio le asigna un puntaje (`RAD=5 … AWFUL=1`). No se agregan ni renombran valores sin actualizar el README raíz, `Docs/api/` y Android.

---

## 🔌 API

Base: `/api/v1` · JSON · campos en `snake_case` · fechas ISO 8601 · IDs UUID.

El contrato oficial está en [`../Docs/api/`](../Docs/api/README.md):

- [`mood-entries.md`](../Docs/api/mood-entries.md)
- [`journal-entries.md`](../Docs/api/journal-entries.md)
- [`auth.md`](../Docs/api/auth.md)
- Estadísticas: se documentan en B5.

| Método | Endpoint | Auth |
|---|---|---|
| `POST` | `/auth/register` | No |
| `POST` | `/auth/login` | No |
| `POST` | `/auth/refresh` | No (envía el refresh token) |
| `POST` | `/auth/logout` | No (envía el refresh token) |
| `GET` | `/mood-entries` | Sí |
| `POST` | `/mood-entries` | Sí |
| `GET` | `/mood-entries/{id}` | Sí |
| `PUT` | `/mood-entries/{id}` | Sí |
| `DELETE` | `/mood-entries/{id}` | Sí |
| `GET` | `/journal-entries` | Sí |
| `POST` | `/journal-entries` | Sí |
| `GET` | `/journal-entries/{id}` | Sí |
| `PUT` | `/journal-entries/{id}` | Sí |
| `DELETE` | `/journal-entries/{id}` | Sí |
| `GET` | `/stats/mood-distribution` | Sí |
| `GET` | `/health` | No |

**Error unificado:**

```json
{ "error": { "code": "VALIDATION_ERROR", "message": "Descripción legible", "details": [] } }
```

**Regla de contrato:** el backend implementa lo que dice `Docs/api/`. Si el contrato es ambiguo o hay que cambiarlo, se actualiza primero ahí y después el código.

---

## 🛠️ Entorno de desarrollo

### Variables de entorno (`.env.example`)

```env
DATABASE_URL=postgresql+psycopg://mimun:mimun@localhost:5432/mimun
TEST_DATABASE_URL=postgresql+psycopg://mimun:mimun@localhost:5432/mimun_test
SECRET_KEY=cambiar-por-un-valor-largo-y-aleatorio
ACCESS_TOKEN_EXPIRE_MINUTES=30
REFRESH_TOKEN_EXPIRE_DAYS=14
ENVIRONMENT=development
AUTH_ENABLED=false
```

En producción (`ENVIRONMENT=production`) la app no arranca si `SECRET_KEY` es la de ejemplo o tiene menos de 32 caracteres, ni si `AUTH_ENABLED` es `false`.

Nunca se commitea `.env`; solo `.env.example`.

### Levantar PostgreSQL (Docker Compose)

```yaml
services:
  db:
    image: postgres:16
    environment:
      POSTGRES_USER: mimun
      POSTGRES_PASSWORD: mimun
      POSTGRES_DB: mimun
    ports:
      - "5432:5432"
    volumes:
      - mimun_pgdata:/var/lib/postgresql/data

volumes:
  mimun_pgdata:
```

```bash
docker compose up -d db
```

### Instalar y correr

```bash
cd Backend
python -m venv .venv
source .venv/bin/activate          # Windows: .venv\Scripts\activate
pip install -r requirements.txt
cp .env.example .env               # Windows: copy .env.example .env
docker compose up -d db            # solo si vas a usar la base de datos
uvicorn app.main:app --reload
```

> Antes de levantar la API por primera vez (y después de cada migración nueva) ejecutar `alembic upgrade head`.

- API: `http://localhost:8000`
- Swagger: `http://localhost:8000/docs`
- Desde el emulador Android: `http://10.0.2.2:8000`

### Migraciones

```bash
alembic revision --autogenerate -m "descripcion del cambio"
alembic upgrade head
alembic downgrade -1
```

Revisar siempre a mano la migración generada antes de aplicarla.

### Tests y calidad

```bash
pytest              # tests
ruff check .        # lint
ruff format .       # formato
lint-imports        # verifica la regla de dependencia entre capas
```

Los tests de integración usan una base de datos separada (o transacciones que se revierten), nunca la de desarrollo.

---

## ✅ Convenciones de código

- Tipado explícito en funciones y métodos.
- Para fechas usar `import datetime as dt` y anotar `dt.date` / `dt.datetime`, así el campo `date` no choca con el tipo.
- Un archivo por recurso en `use_cases`, `repositories` y `api/v1`.
- Nombres de tablas en plural y `snake_case`; modelos en singular y `PascalCase`.
- Nada de secretos en el código ni en git.
- Commits pequeños: `feat(backend): ...`, `fix(backend): ...`, `docs(backend): ...`.
- Cada endpoint y cada caso de uso nuevo incluye tests.

---

## 🤖 Reglas para el agente Backend

1. Leer el README raíz y este README antes de hacer cambios estructurales.
2. Trabajar solo dentro de `Backend/`, `Docs/` (contrato, base de datos y decisiones) y `Auth/` cuando se defina su rol. No tocar `Frontend/`.
3. Implementar exactamente lo que dice `Docs/api/`. Si falta o es ambiguo, definirlo ahí primero.
4. Todo cambio de esquema incluye migración de Alembic y actualización de `Docs/database/`.
5. Respetar Clean Architecture: la dependencia apunta siempre hacia `domain`.
6. No asumir que existe algo que no figura en "Estado actual".
7. Mantener los estados emocionales idénticos a los definidos.
8. Priorizar código simple, mantenible y comprensible.
