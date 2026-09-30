# 🔌 Mimun — Contrato de la API

Fuente de verdad de la API REST. **El backend implementa lo que dice acá y el Frontend consume lo que dice acá.** Si algo es ambiguo o hay que cambiarlo, se edita este documento primero y después el código.

**Estado:** 🟢 v1 aprobada por el autor (2026-09-29). Se puede ajustar durante B3 y B4 si el Frontend lo necesita; todo cambio se documenta acá primero.

| Recurso | Documento |
|---|---|
| Registros emocionales | [mood-entries.md](./mood-entries.md) |
| Reflexiones | [journal-entries.md](./journal-entries.md) |
| Autenticación | [auth.md](./auth.md) |
| Estadísticas | ⚪ se documenta en la Fase B5 |

---

## Convenciones generales

| Tema | Regla |
|---|---|
| Base URL | `/api/v1` (emulador Android: `http://10.0.2.2:8000/api/v1`) |
| Formato | JSON (`Content-Type: application/json`), UTF-8 |
| Nombres de campos | `snake_case` |
| IDs | UUID v4 en formato texto. **Los puede generar el cliente.** |
| Fechas | `date`: `YYYY-MM-DD`. Instantes: ISO 8601 en UTC con sufijo `Z` (`2026-09-29T14:30:00Z`). |
| Campos desconocidos en el request | Se rechazan con `422`. |
| Campos opcionales sin valor | Se devuelven como `null`, no se omiten. |

### Autenticación

Los endpoints de datos usan `Authorization: Bearer <access_token>`.

> **Hasta la Fase B6 no hay login real.** Durante B3 y B4 el backend asigna todas las peticiones a un usuario fijo de desarrollo y no exige el header. El contrato de los endpoints de datos **no cambia** cuando se active la auth: solo se vuelve obligatorio el header.

### Estados emocionales (`mood`)

Valores exactos, en mayúsculas, idénticos a `MoodType` en Android:

`RAD` · `GOOD` · `MEH` · `BAD` · `AWFUL`

Cualquier otro valor (incluido `NORMAL`) responde `422`.

### Formato de error unificado

Toda respuesta de error usa esta forma:

```json
{
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Descripción legible",
    "details": [
      { "field": "mood", "message": "Debe ser uno de: RAD, GOOD, MEH, BAD, AWFUL" }
    ]
  }
}
```

- `code`: identificador estable en `MAYUSCULAS_CON_GUION_BAJO`. El cliente decide su comportamiento con este campo, **no** con `message`.
- `message`: texto legible, solo para depuración.
- `details`: lista posible vacía. Cada elemento tiene `field` y `message`.

| HTTP | `code` | Cuándo |
|---|---|---|
| `401` | `UNAUTHORIZED` | Falta el token, es inválido o venció (desde B6). |
| `403` | `FORBIDDEN` | Autenticado pero sin permiso (desde B6). |
| `404` | `NOT_FOUND` | El recurso no existe **o pertenece a otro usuario**. |
| `409` | `MOOD_ENTRY_ALREADY_EXISTS` | Ya hay un registro emocional para esa fecha. |
| `409` | `EMAIL_ALREADY_REGISTERED` | Ya existe una cuenta con ese email. |
| `422` | `VALIDATION_ERROR` | Body o query params inválidos, incluido JSON malformado. |
| `500` | `INTERNAL_ERROR` | Error inesperado. Nunca expone detalles internos. |

> Un recurso ajeno responde `404` y no `403`, para no revelar que existe.

### Códigos de éxito

| HTTP | Uso |
|---|---|
| `200` | Lectura o actualización correcta. |
| `201` | Recurso creado. |
| `204` | Borrado correcto, sin cuerpo. |

### Paginación

Los listados aceptan `limit` y `offset` y responden con esta envoltura:

```json
{
  "items": [],
  "total": 0,
  "limit": 20,
  "offset": 0
}
```

`total` es la cantidad de elementos que cumplen el filtro, sin contar la paginación.

### Health check

`GET /api/v1/health` → `200`, sin auth:

```json
{ "status": "ok" }
```
