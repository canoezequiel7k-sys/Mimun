# 🖥️ Mimun — Backend

API REST de Mimun, hecha con **Python + FastAPI + PostgreSQL**.

> Antes de trabajar acá, leer el [README raíz](../README.md). Este documento detalla solo el backend. Si algo contradice al README raíz, gana el raíz.
> El plan de trabajo está en [ROADMAP.md](./ROADMAP.md). El contrato de la API vive en [`../Docs/api/`](../Docs/api/).

---

## 📍 Estado actual

🔴 **No iniciado.** No existe código, base de datos ni migraciones. Todo lo descripto acá es la arquitectura objetivo.

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
| Validación | Pydantic v2 |
| Configuración | pydantic-settings (`.env`) |
| Auth | JWT + hash de contraseñas (argon2 o bcrypt) |
| Tests | pytest + httpx |
| Contenedores | Docker Compose (para PostgreSQL en desarrollo) |

---

## 🗂️ Estructura

```
Backend/
├── app/
│   ├── api/
│   │   ├── deps.py            # Dependencias (sesión DB, usuario actual)
│   │   └── v1/
│   │       ├── auth.py
│   │       ├── mood_entries.py
│   │       ├── journal_entries.py
│   │       └── stats.py
│   ├── core/
│   │   ├── config.py          # Settings desde variables de entorno
│   │   ├── security.py        # Hash, JWT
│   │   └── errors.py          # Excepciones y formato de error unificado
│   ├── db/
│   │   ├── base.py            # Base declarativa
│   │   └── session.py         # Engine y sesión
│   ├── models/                # Modelos SQLAlchemy
│   ├── schemas/               # Schemas Pydantic (request / response)
│   ├── services/              # Lógica de negocio
│   ├── repositories/          # Acceso a datos
│   └── main.py
├── alembic/                   # Migraciones
├── tests/
│   ├── api/
│   ├── services/
│   └── conftest.py
├── .env.example
├── docker-compose.yml
├── alembic.ini
├── requirements.txt
├── ROADMAP.md
└── README.md
```

---

## 🧱 Capas y reglas

```
Router (api)  →  Service  →  Repository  →  PostgreSQL
   ↑ schemas         ↑ lógica       ↑ SQLAlchemy
```

| Capa | Hace | No hace |
|---|---|---|
| **api** | Recibe HTTP, valida con schemas, delega al service, devuelve respuesta. | Lógica de negocio, consultas SQL. |
| **services** | Reglas de negocio (ej.: un solo registro por día). | Conocer HTTP, `Request`/`Response`. |
| **repositories** | Consultas y escritura en la DB. | Reglas de negocio. |
| **models** | Definen tablas. | Salir directamente en la API. |
| **schemas** | Definen la forma de la API. | Contener lógica de base de datos. |

Reglas adicionales:

- Los modelos SQLAlchemy **nunca** se devuelven directo: siempre se convierten a schemas.
- Todo cambio de esquema requiere **migración de Alembic** y actualizar `Docs/database/`.
- Cada usuario solo accede a **sus propios datos**: todas las consultas filtran por `user_id`.
- Nada de secretos en el código ni en git.

---

## 🗄️ Modelo de datos (propuesta inicial)

> Propuesta a confirmar. La versión definitiva vive en [`../Docs/database/`](../Docs/database/).

### `users`

| Columna | Tipo | Notas |
|---|---|---|
| `id` | UUID | PK |
| `email` | text | único, obligatorio |
| `password_hash` | text | obligatorio |
| `created_at` | timestamptz | UTC |
| `updated_at` | timestamptz | UTC |

### `mood_entries`

| Columna | Tipo | Notas |
|---|---|---|
| `id` | UUID | PK (puede generarlo el cliente para sincronización offline) |
| `user_id` | UUID | FK → `users.id`, `ON DELETE CASCADE` |
| `date` | date | día del registro |
| `mood` | enum `mood_type` | `RAD, GOOD, NORMAL, MEH, BAD, AWFUL` |
| `note` | text | opcional, nota corta |
| `created_at` | timestamptz | UTC |
| `updated_at` | timestamptz | UTC |

Restricción: `UNIQUE (user_id, date)`. Índice: `(user_id, date)`.

### `journal_entries`

| Columna | Tipo | Notas |
|---|---|---|
| `id` | UUID | PK |
| `user_id` | UUID | FK → `users.id`, `ON DELETE CASCADE` |
| `mood_entry_id` | UUID | FK → `mood_entries.id`, opcional, `ON DELETE SET NULL` |
| `title` | text | opcional |
| `content` | text | obligatorio |
| `created_at` | timestamptz | UTC |
| `updated_at` | timestamptz | UTC |

Índice: `(user_id, created_at DESC)`.

### Relaciones

```
users 1 ─── N mood_entries
users 1 ─── N journal_entries
mood_entries 1 ─── 0..N journal_entries
```

---

## 😊 Estados emocionales

Los valores son **exactamente** estos, en mayúsculas y en inglés, idénticos en todas las capas:

`RAD`, `GOOD`, `NORMAL`, `MEH`, `BAD`, `AWFUL`

Se definen una sola vez en un `Enum` de Python y se reutilizan en modelos, schemas y tests. No se agregan ni renombran valores sin actualizar el README raíz, la API y Android.

---

## 🔌 API

Base: `/api/v1` · JSON · campos en `snake_case` · fechas ISO 8601.

El contrato oficial está en [`../Docs/api/`](../Docs/api/). Resumen del borrador inicial:

| Método | Endpoint | Auth |
|---|---|---|
| `POST` | `/auth/register` | No |
| `POST` | `/auth/login` | No |
| `POST` | `/auth/refresh` | Refresh token |
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

**Códigos HTTP:** `200`, `201`, `204`, `400`, `401`, `403`, `404`, `409` (ya existe un registro ese día), `422`.

**Regla de contrato:** el backend implementa lo que dice `Docs/api/`. Si el contrato es ambiguo o hay que cambiarlo, se actualiza primero ahí y después el código.

---

## 🛠️ Entorno de desarrollo

### Variables de entorno (`.env.example`)

```env
DATABASE_URL=postgresql+psycopg://mimun:mimun@localhost:5432/mimun
SECRET_KEY=cambiar-por-un-valor-largo-y-aleatorio
ACCESS_TOKEN_EXPIRE_MINUTES=30
REFRESH_TOKEN_EXPIRE_DAYS=14
ENVIRONMENT=development
```

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
cp .env.example .env
alembic upgrade head
uvicorn app.main:app --reload
```

- API: `http://localhost:8000`
- Documentación interactiva (Swagger): `http://localhost:8000/docs`
- Desde el emulador Android: `http://10.0.2.2:8000`

### Migraciones

```bash
alembic revision --autogenerate -m "descripcion del cambio"
alembic upgrade head
alembic downgrade -1
```

Revisar siempre a mano la migración generada antes de aplicarla.

### Tests

```bash
pytest
```

Los tests usan una base de datos separada (o transacciones que se revierten), nunca la de desarrollo.

---

## ✅ Convenciones de código

- Tipado explícito en funciones y métodos.
- Un archivo por recurso en `api/v1`, `services`, `repositories`.
- Nombres de tablas en plural y `snake_case`; modelos en singular y `PascalCase`.
- Formato y lint con `ruff`.
- Commits pequeños: `feat(backend): ...`, `fix(backend): ...`, `docs(backend): ...`.
- Cada endpoint nuevo incluye tests.

---

## 🤖 Reglas para el agente Backend

1. Leer el README raíz y este README antes de hacer cambios estructurales.
2. Trabajar solo dentro de `Backend/` (y `Auth/` cuando se defina su rol). No tocar `Frontend/`.
3. Implementar exactamente lo que dice `Docs/api/`. Si falta o es ambiguo, definirlo ahí primero.
4. Todo cambio de esquema incluye migración de Alembic y actualización de `Docs/database/`.
5. Respetar las capas: `api → services → repositories`.
6. No asumir que existe algo que no figura en "Estado actual".
7. Mantener los estados emocionales idénticos a los definidos.
8. Priorizar código simple, mantenible y comprensible.
