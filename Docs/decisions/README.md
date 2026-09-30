# 🧭 Mimun — Registro de decisiones

Decisiones de arquitectura y de producto que afectan a más de un módulo. Se agregan al final; no se borran: si una decisión cambia, se marca como **Reemplazada** y se agrega la nueva.

**Estados:** ✅ Aceptada · 🟡 Propuesta (pendiente de confirmar por el autor) · ⛔ Reemplazada

| # | Decisión | Estado |
|---|---|---|
| 0001 | Clean Architecture estricta en el backend | ✅ |
| 0002 | Cinco estados emocionales, sin `NORMAL` | ✅ |
| 0003 | IDs UUID generados por el cliente | ✅ |
| 0004 | Un registro por día, con `date` enviada por el cliente | ✅ |
| 0005 | `mood` guardado como `text` + `CHECK` | ✅ |
| 0006 | Rol de `Auth/`: módulo dentro del backend | ✅ |
| 0007 | Un recurso ajeno responde `404` | ✅ |
| 0008 | Sesión: JWT de acceso + refresh token opaco con rotación, y `AUTH_ENABLED` | 🟡 |

---

## 0001 — Clean Architecture estricta en el backend ✅

**Decisión:** el backend se organiza en `domain`, `application`, `infrastructure` y `presentation`, con la dependencia apuntando siempre hacia `domain`. La regla se verifica automáticamente con `import-linter`.

**Motivo:** pedido explícito del autor. Además, replica el criterio del Android (`presentation → domain ← data`), lo que hace que ambos lados sean fáciles de razonar juntos.

**Consecuencias:** más archivos y algo de código de mapeo entre entidades, modelos y schemas. A cambio, la lógica de negocio se prueba sin base de datos ni HTTP, y se puede cambiar FastAPI o SQLAlchemy sin tocar las reglas.

Detalle en [`Backend/README.md`](../../Backend/README.md).

---

## 0002 — Cinco estados emocionales, sin `NORMAL` ✅

**Decisión:** los estados son `RAD, GOOD, MEH, BAD, AWFUL`. `NORMAL` se eliminó por decisión del autor.

**Motivo:** decisión de producto del autor. Ya está reflejada en `MoodType.kt` de Android.

**Consecuencias:** los valores son idénticos en Android, API, base de datos y tests. Cualquier cambio requiere actualizar el README raíz, `Docs/api/` y Android.

**Pendiente:** `Frontend/README.md` todavía lista `NORMAL`. Debe corregirlo quien trabaje el Frontend.

---

## 0003 — IDs UUID generados por el cliente ✅

**Decisión:** `mood_entries` y `journal_entries` usan UUID como clave primaria, y el cliente puede generarlo.

**Motivo:** permite crear datos offline y sincronizarlos después sin colisiones. Android ya genera `UUID.randomUUID()` en `MoodEntry`.

**Consecuencias:** el servidor acepta un `id` opcional en `POST`. La idempotencia de reintentos se define en la Fase B7.

---

## 0004 — Un registro por día, con `date` enviada por el cliente ✅

**Decisión:** un usuario tiene como máximo un `MoodEntry` por día (`UNIQUE (user_id, date)`). La `date` la envía el cliente, según la fecha **local** del usuario. El servidor no la deriva de la hora UTC. `POST` sobre un día ya ocupado responde `409`; para cambiarlo se usa `PUT`.

**Motivo:** "un registro por día" es una regla de producto ya documentada. Android hoy guarda un `timestamp` (`LocalDateTime`), y derivar el día en el servidor con UTC registraría mal las noches de Argentina (UTC−3).

**Consecuencias:** Android envía `date = timestamp.toLocalDate()`. Se acepta hasta un día de tolerancia hacia adelante. Cambiar de día un registro requiere borrar y crear.

**Confirmada por el autor (2026-09-29):** el backend guarda solo el día, no la hora. Si más adelante se quiere la hora exacta, se agrega una columna `logged_at` que se envía además de `date`.

---

## 0005 — `mood` guardado como `text` + `CHECK` ✅

**Decisión:** la columna `mood` es `text` con `CHECK (mood IN (...))`, en lugar de un enum nativo de PostgreSQL.

**Motivo:** los enums nativos son incómodos de modificar con Alembic (agregar o quitar valores exige operaciones especiales, y quitar uno es difícil). Los estados ya cambiaron una vez (se quitó `NORMAL`), así que conviene que sea barato volver a cambiarlos.

**Consecuencias:** la validación fuerte sigue existiendo (por el `CHECK` y por el `Enum` de Python en `domain`). Cambiar los valores es una migración simple.

---

## 0006 — Rol de `Auth/`: módulo dentro del backend ✅

**Decisión:** la autenticación se implementa **como un módulo del mismo backend** (casos de uso `auth/`, endpoints `/auth/*`), no como un servicio separado. La carpeta `Auth/` queda sin uso: se elimina, o se reserva para documentación de auth si hace falta.

**Motivo:** un servicio de auth independiente agrega despliegue, comunicación entre servicios y un segundo punto de falla, sin necesidad real para una app de este tamaño. Con Clean Architecture, extraerlo más adelante es viable porque está aislado en sus propios casos de uso y ports.

**Consecuencias:** un solo proceso y una sola base de datos. Si en el futuro se quiere un proveedor externo (por ejemplo Google Sign-In), se agrega otro adaptador de infraestructura sin tocar el dominio.

**Confirmada por el autor (2026-09-29).** La carpeta `Auth/` queda sin uso y puede eliminarse.

---

## 0007 — Un recurso ajeno responde `404` ✅

**Decisión:** si un usuario pide un recurso que existe pero es de otro, la API responde `404 NOT_FOUND`, igual que si no existiera.

**Motivo:** responder `403` confirmaría que el ID existe. Es un dato personal sensible (una app de diario emocional).

**Consecuencias:** `403` queda reservado para permisos, no para propiedad de datos.

---

## 0008 — Sesión: JWT de acceso + refresh token opaco con rotación, y `AUTH_ENABLED` 🟡

**Decisión:** el `access_token` es un JWT de vida corta (30 min). El `refresh_token` es un texto aleatorio opaco; el servidor guarda solo su hash SHA-256 en `refresh_tokens` y lo **rota** en cada uso. Los repositorios hacen `commit` por operación. Una variable `AUTH_ENABLED` permite trabajar sin token en desarrollo; es obligatoria (`true`) en producción y la app se niega a arrancar si no.

**Motivo:** un refresh token revocable permite cerrar sesión de verdad y limita el daño si se filtra. Con datos tan sensibles como un diario emocional, conviene no depender solo de JWT largos. `AUTH_ENABLED` evita bloquear la Fase F6 del Frontend (integración de mood y journal) mientras todavía no existe la pantalla de login de F7.

**Consecuencias:** una tabla más y una consulta a la base por cada refresh. Los access tokens no se pueden revocar antes de que venzan (por eso duran poco). Detalle en [`Docs/api/auth.md`](../api/auth.md).
