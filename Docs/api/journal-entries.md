# 📝 API — Reflexiones (`journal-entries`)

Convenciones generales, errores y paginación en [README.md](./README.md).

**Estado:** 🟡 Borrador v1 (Fase B0). Android todavía no implementa esta feature (Fase F3), así que el contrato se puede ajustar según lo que necesite la UI.

> ⚠️ **Desde la Fase B7 rige [sync.md](./sync.md)**, que modifica este documento: los objetos incluyen `edited_at` y `deleted_at`, `PUT /{id}` es un *upsert* (crea si no existe), `DELETE` es un borrado lógico idempotente, el listado acepta `updated_since` y la creación acepta `created_at`. Donde difieran, gana `sync.md`.

---

## Objeto `JournalEntry`

```json
{
  "id": "a8d1f0c2-77b3-4b8e-8f0a-52c1e3d94a60",
  "mood_entry_id": "3f2b8c1e-6a4d-4e0b-9c55-1d2a7e9f0b11",
  "title": "Sobre el trabajo",
  "content": "Hoy pude cerrar la entrega y me sentí aliviado.",
  "created_at": "2026-09-29T21:10:00Z",
  "updated_at": "2026-09-29T21:10:00Z"
}
```

| Campo | Tipo | Notas |
|---|---|---|
| `id` | UUID | Lo puede generar el cliente. |
| `mood_entry_id` | UUID \| null | Registro emocional asociado. Opcional. |
| `title` | string \| null | Opcional. Hasta 120 caracteres. |
| `content` | string | Obligatorio. De 1 a 10.000 caracteres. |
| `created_at` | instante UTC | Lo asigna el servidor. |
| `updated_at` | instante UTC | Lo asigna el servidor. |

Una reflexión puede existir sin registro emocional. Varias reflexiones pueden apuntar al mismo registro.

---

## `POST /journal-entries`

**Request**

```json
{
  "id": "a8d1f0c2-77b3-4b8e-8f0a-52c1e3d94a60",
  "mood_entry_id": "3f2b8c1e-6a4d-4e0b-9c55-1d2a7e9f0b11",
  "title": "Sobre el trabajo",
  "content": "Hoy pude cerrar la entrega y me sentí aliviado."
}
```

| Campo | Obligatorio | Reglas |
|---|---|---|
| `id` | No | UUID. Si falta, lo genera el servidor. |
| `mood_entry_id` | No | Debe existir y ser del mismo usuario. |
| `title` | No | Máximo 120 caracteres. Se recorta; si queda vacío se guarda `null`. |
| `content` | Sí | De 1 a 10.000 caracteres, sin contar espacios de los extremos. |

| HTTP | Cuándo |
|---|---|
| `201` | Creada. Devuelve el objeto `JournalEntry`. |
| `422` `VALIDATION_ERROR` | Campos inválidos, o `mood_entry_id` inexistente o ajeno (el error apunta al campo `mood_entry_id`). |

---

## `GET /journal-entries`

Lista las reflexiones del usuario, ordenadas por `created_at` descendente.

| Query param | Tipo | Default | Notas |
|---|---|---|---|
| `mood_entry_id` | UUID | — | Filtra las reflexiones de un registro emocional. |
| `limit` | entero | `20` | De 1 a 100. |
| `offset` | entero | `0` | Mayor o igual a 0. |

**Respuesta `200`**

```json
{
  "items": [],
  "total": 0,
  "limit": 20,
  "offset": 0
}
```

---

## `GET /journal-entries/{id}`

`200` con el objeto `JournalEntry`, o `404 NOT_FOUND`.

---

## `PUT /journal-entries/{id}`

Reemplazo completo de los campos editables. Lo que no se envía queda en `null` (excepto `content`, que es obligatorio).

**Request**

```json
{
  "mood_entry_id": null,
  "title": "Sobre el trabajo (editado)",
  "content": "Texto actualizado."
}
```

| HTTP | Cuándo |
|---|---|
| `200` | Devuelve el objeto actualizado (`updated_at` cambia). |
| `404` `NOT_FOUND` | No existe o es de otro usuario. |
| `422` `VALIDATION_ERROR` | Campos inválidos. |

---

## `DELETE /journal-entries/{id}`

| HTTP | Cuándo |
|---|---|
| `204` | Borrada. |
| `404` `NOT_FOUND` | No existe o es de otro usuario. |
