# 🔄 API — Sincronización (offline-first)

Convenciones generales, errores y paginación en [README.md](./README.md). Este documento **extiende** [mood-entries.md](./mood-entries.md) y [journal-entries.md](./journal-entries.md); donde difiere, **gana este**.

**Estado:** 🟢 v1 implementada y probada en la Fase B7 (2026-09-30).

---

## Dos fechas con dos propósitos

| Campo | Quién lo asigna | Para qué sirve |
|---|---|---|
| `updated_at` | **El servidor**, en cada escritura aceptada | Solo como **cursor de descarga** (`updated_since`). El cliente nunca lo envía. |
| `edited_at` | **El cliente**: cuándo el usuario editó | **Resolver conflictos.** Se mapea desde el `updatedAt` local de Room. |

> Si el cliente usara su propia fecha como cursor, un cambio hecho offline a las 10:00 y subido a las 12:00 quedaría "en el pasado" y nadie lo descargaría. Por eso son dos.

- `edited_at` es opcional. Si falta, vale la hora del servidor. Debe llevar zona horaria (`...Z`); sin ella: `422`.
- El servidor **acota** `edited_at` a su propia hora actual: un reloj adelantado no puede ganar todos los conflictos.
- Todas las fechas son instantes UTC con sufijo `Z`.

## Cambios en los objetos

`MoodEntry` y `JournalEntry` ganan dos campos (siempre presentes). `JournalEntry` también devuelve `icon` (siempre presente, nullable):

| Campo | Tipo | Notas |
|---|---|---|
| `edited_at` | instante UTC | Ver arriba. |
| `deleted_at` | instante UTC \| null | `null` en registros activos. Solo es no nulo en las respuestas de **descarga** (`updated_since`) y de `PUT` cuando gana un borrado. |
| `icon` (solo `JournalEntry`) | string \| null | Identificador visual opcional: `apple`, `sun`, `sky` o `great_v2`. Se conserva al crear, actualizar y sincronizar. |

El cliente debe **ignorar campos desconocidos** al deserializar.

`JournalEntry` acepta además `created_at` opcional **al crear** (también acotado a la hora del servidor), para conservar la hora real en que se escribió la reflexión aunque se sincronice después.

---

## `PUT /mood-entries/{id}` y `PUT /journal-entries/{id}` (upsert)

Crea o actualiza según el `id`. **Es idempotente**: reenviar lo mismo es seguro.

**`mood-entries`** — body: `mood` (obligatorio), `note`, `date`, `edited_at`.
**`journal-entries`** — body: `content` (obligatorio), `title`, `icon`, `mood_entry_id`, `created_at`, `edited_at`.

| Situación | Resultado |
|---|---|
| El `id` no existe y se envía `date` (mood) / siempre (journal) | Se crea. `201` con el objeto. |
| El `id` pertenece a otro usuario | `404 NOT_FOUND`, sin revelar que el registro existe. |
| El `id` no existe y es un mood **sin** `date` | `404 NOT_FOUND`. |
| El `id` existe y el `edited_at` entrante es **mayor o igual** al guardado | Se aplica. `200` con el objeto actualizado. Si estaba borrado, **se revive** (`deleted_at` pasa a `null`). |
| El `id` existe y el `edited_at` entrante es **menor** al guardado | **No se aplica.** `200` con la **versión del servidor**: el cliente la adopta. Si es un borrado, `deleted_at` viene con valor y el cliente borra localmente. |
| Se envía una `date` distinta a la del registro existente | `422` (la fecha es inmutable). |
| Ya hay **otro** registro activo para esa `date` | `409 MOOD_ENTRY_ALREADY_EXISTS`. El cliente debe unificarlos (gana el de mayor `edited_at`) y borrar el perdedor. |

La respuesta de un `PUT` es **siempre la verdad del servidor**: el cliente la guarda como estado final.

---

## `DELETE /mood-entries/{id}` y `DELETE /journal-entries/{id}` (borrado lógico)

Marca `deleted_at`, y también actualiza `updated_at` y `edited_at` (con la hora del servidor).

| HTTP | Cuándo |
|---|---|
| `204` | Borrado. **Idempotente**: borrar algo ya borrado también devuelve `204`. |
| `404` `NOT_FOUND` | El `id` nunca existió o es de otro usuario. |

- `GET /{id}` y los listados normales **no** devuelven registros borrados (`404` / ausentes).
- Al borrar un registro emocional, sus reflexiones **quedan sin asociación** (`mood_entry_id = null`) y su `updated_at` se actualiza, así se descargan en el próximo `pull`.
- El día de un registro borrado **queda libre** para crear otro.

---

## Descarga de cambios: `GET /mood-entries?updated_since=...`

Igual para `/journal-entries`.

| Query param | Notas |
|---|---|
| `updated_since` | Instante con zona horaria. Devuelve **todos** los registros con `updated_at` **mayor** (estricto), **incluidos los borrados**. |
| `limit`, `offset` | Mismos rangos que el listado normal. |

- Orden: `updated_at` **ascendente**.
- **No se puede combinar** con `from`, `to` (mood) ni `mood_entry_id` (journal): `422`.
- Sin `updated_since`, el listado es el de siempre.

---

## Algoritmo sugerido para el cliente

1. **Subir (push):** por cada registro local pendiente (`syncStatus = PENDING`): si se borró localmente, `DELETE`; si no, `PUT` con `edited_at` = su `updatedAt` local. Guardar **la respuesta** como estado final y marcarlo `SYNCED` (si trae `deleted_at`, borrarlo localmente).
2. **Bajar (pull):** `GET ?updated_since=<cursor>` repetido con `offset` hasta que `items` traiga menos que `limit`. Aplicar cada ítem localmente (crear, actualizar o borrar). Guardar como nuevo cursor el mayor `updated_at` visto. La primera vez, sin `updated_since`.
3. Recibir un ítem ya conocido es inofensivo: aplicarlo es idempotente.
4. Orden recomendado: sincronizar **primero los registros emocionales** y después las reflexiones.
