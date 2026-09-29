# 🗄️ Mimun — Esquema de base de datos

Modelo de datos de PostgreSQL. Fuente de verdad del esquema: **todo cambio se refleja acá y en una migración de Alembic**.

**Estado:** 🟡 Borrador v1 (Fase B0). Todavía no hay migraciones.

Contexto de arquitectura en [`../../Backend/README.md`](../../Backend/README.md). Decisiones relacionadas en [`../decisions/`](../decisions/README.md).

---

## Convenciones

- Tablas en plural y `snake_case`. Columnas en `snake_case`.
- Claves primarias `uuid`. Los IDs de `mood_entries` y `journal_entries` pueden venir generados por el cliente.
- Todos los instantes son `timestamptz` en UTC.
- `created_at` y `updated_at` en todas las tablas. `updated_at` se actualiza en cada modificación.
- Los valores emocionales se guardan como `text` con `CHECK`, **no** como enum nativo de PostgreSQL (decisión 0005).
- Nada se borra en cascada hacia el usuario salvo lo indicado.

---

## Diagrama

```
users 1 ─── N mood_entries
users 1 ─── N journal_entries
mood_entries 1 ─── 0..N journal_entries
```

---

## `users`

| Columna | Tipo | Restricciones |
|---|---|---|
| `id` | `uuid` | PK |
| `email` | `text` | `NOT NULL` |
| `password_hash` | `text` | `NOT NULL` |
| `created_at` | `timestamptz` | `NOT NULL`, default `now()` |
| `updated_at` | `timestamptz` | `NOT NULL`, default `now()` |

Índice único: `UNIQUE (lower(email))`. El email se guarda normalizado en minúsculas.

---

## `mood_entries`

| Columna | Tipo | Restricciones |
|---|---|---|
| `id` | `uuid` | PK |
| `user_id` | `uuid` | `NOT NULL`, FK → `users.id` `ON DELETE CASCADE` |
| `date` | `date` | `NOT NULL`. Fecha local del usuario, enviada por el cliente. |
| `mood` | `text` | `NOT NULL`, `CHECK (mood IN ('RAD','GOOD','MEH','BAD','AWFUL'))` |
| `note` | `text` | Nulable. `CHECK (char_length(note) <= 500)` |
| `created_at` | `timestamptz` | `NOT NULL`, default `now()` |
| `updated_at` | `timestamptz` | `NOT NULL`, default `now()` |

Restricciones e índices:

- `UNIQUE (user_id, date)`: un registro por usuario y día. También sirve como índice de consulta por rango.

---

## `journal_entries`

| Columna | Tipo | Restricciones |
|---|---|---|
| `id` | `uuid` | PK |
| `user_id` | `uuid` | `NOT NULL`, FK → `users.id` `ON DELETE CASCADE` |
| `mood_entry_id` | `uuid` | Nulable, FK → `mood_entries.id` `ON DELETE SET NULL` |
| `title` | `text` | Nulable. `CHECK (char_length(title) <= 120)` |
| `content` | `text` | `NOT NULL`. `CHECK (char_length(content) BETWEEN 1 AND 10000)` |
| `created_at` | `timestamptz` | `NOT NULL`, default `now()` |
| `updated_at` | `timestamptz` | `NOT NULL`, default `now()` |

Índices:

- `(user_id, created_at DESC)`: listado paginado.
- `(mood_entry_id)`: filtro por registro emocional.

---

## Reglas de integridad

- Cada consulta filtra por `user_id`. Ningún repositorio expone un método sin ese parámetro.
- Que `journal_entries.mood_entry_id` pertenezca al mismo usuario **no lo garantiza la base de datos**: lo valida el caso de uso.
- La unicidad `(user_id, date)` es la garantía final ante condiciones de carrera; el caso de uso además la verifica para devolver el error `409` con el código correcto.

---

## Pendiente para fases posteriores

| Tema | Fase |
|---|---|
| Borrado lógico (`deleted_at`) en `mood_entries` y `journal_entries` | B7 |
| Tabla de refresh tokens (revocación) | B6 |
| Campos para sincronización (`updated_since`) | B7 |
