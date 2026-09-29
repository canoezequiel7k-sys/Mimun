# 😊 API — Registros emocionales (`mood-entries`)

Convenciones generales, errores y paginación en [README.md](./README.md).

**Estado:** 🟡 Borrador v1 (Fase B0).

Un usuario tiene **como máximo un registro por día** (`UNIQUE (user_id, date)`).

---

## Objeto `MoodEntry`

```json
{
  "id": "3f2b8c1e-6a4d-4e0b-9c55-1d2a7e9f0b11",
  "date": "2026-09-29",
  "mood": "GOOD",
  "note": "Buen día en el trabajo",
  "created_at": "2026-09-29T14:30:00Z",
  "updated_at": "2026-09-29T14:30:00Z"
}
```

| Campo | Tipo | Notas |
|---|---|---|
| `id` | UUID | Lo puede generar el cliente. |
| `date` | `YYYY-MM-DD` | Día del registro, **según la fecha local del usuario**. Inmutable una vez creado. |
| `mood` | string | `RAD`, `GOOD`, `MEH`, `BAD` o `AWFUL`. |
| `note` | string \| null | Opcional. Hasta 500 caracteres. |
| `created_at` | instante UTC | Lo asigna el servidor. |
| `updated_at` | instante UTC | Lo asigna el servidor. |

### Correspondencia con Android

| Android (`MoodEntry`) | API |
|---|---|
| `id: String` (UUID) | `id` |
| `moodType: MoodType` | `mood` (mismo nombre del enum) |
| `note: String?` | `note` |
| `timestamp: LocalDateTime` | `date` = `timestamp.toLocalDate()` |

El cliente envía la **fecha local** del usuario. El servidor no la deriva de la hora UTC, así evita que alguien en Argentina registre el día equivocado a la noche.

---

## `POST /mood-entries`

Crea un registro.

**Request**

```json
{
  "id": "3f2b8c1e-6a4d-4e0b-9c55-1d2a7e9f0b11",
  "date": "2026-09-29",
  "mood": "GOOD",
  "note": "Buen día en el trabajo"
}
```

| Campo | Obligatorio | Reglas |
|---|---|---|
| `id` | No | UUID. Si falta, lo genera el servidor. |
| `date` | Sí | No puede ser posterior a mañana (UTC), para tolerar husos horarios. |
| `mood` | Sí | Uno de los cinco valores. |
| `note` | No | Máximo 500 caracteres. Se recorta (`trim`); si queda vacía se guarda como `null`. |

**Respuestas**

| HTTP | Cuándo |
|---|---|
| `201` | Creado. Devuelve el objeto `MoodEntry`. |
| `409` `MOOD_ENTRY_ALREADY_EXISTS` | Ya existe un registro para ese `date`. Para cambiarlo, usar `PUT`. |
| `422` `VALIDATION_ERROR` | Campos inválidos, `id` mal formado o `id` ya usado. |

> La idempotencia ante reintentos de `POST` (mismo `id` enviado dos veces) se define en la Fase B7.

---

## `GET /mood-entries`

Lista los registros del usuario, ordenados por `date` descendente.

| Query param | Tipo | Default | Notas |
|---|---|---|---|
| `from` | `YYYY-MM-DD` | — | Inclusive. |
| `to` | `YYYY-MM-DD` | — | Inclusive. Debe ser mayor o igual a `from`. |
| `limit` | entero | `100` | De 1 a 366. |
| `offset` | entero | `0` | Mayor o igual a 0. |

**Respuesta `200`**

```json
{
  "items": [
    {
      "id": "3f2b8c1e-6a4d-4e0b-9c55-1d2a7e9f0b11",
      "date": "2026-09-29",
      "mood": "GOOD",
      "note": null,
      "created_at": "2026-09-29T14:30:00Z",
      "updated_at": "2026-09-29T14:30:00Z"
    }
  ],
  "total": 1,
  "limit": 100,
  "offset": 0
}
```

**Registro de hoy:** `GET /mood-entries?from=2026-09-29&to=2026-09-29`. Si `items` está vacío, hoy no hay registro. No existe un endpoint aparte.

Errores: `422` si `from > to` o algún param es inválido.

---

## `GET /mood-entries/{id}`

`200` con el objeto `MoodEntry`, o `404 NOT_FOUND`.

---

## `PUT /mood-entries/{id}`

Reemplaza el `mood` y la `note` de un registro. **La `date` no se modifica**: para cambiar de día hay que borrar y crear.

**Request**

```json
{
  "mood": "MEH",
  "note": null
}
```

| Campo | Obligatorio | Reglas |
|---|---|---|
| `mood` | Sí | Uno de los cinco valores. |
| `note` | No | Si falta o es `null`, la nota se borra. Mismas reglas que en `POST`. |

`PUT` es un reemplazo completo: lo que no se envía queda en `null`.

| HTTP | Cuándo |
|---|---|
| `200` | Devuelve el objeto actualizado (`updated_at` cambia). |
| `404` `NOT_FOUND` | No existe o es de otro usuario. |
| `422` `VALIDATION_ERROR` | Campos inválidos. |

---

## `DELETE /mood-entries/{id}`

| HTTP | Cuándo |
|---|---|
| `204` | Borrado. |
| `404` `NOT_FOUND` | No existe o es de otro usuario. |

Las reflexiones asociadas **no se borran**: quedan con `mood_entry_id = null`.
