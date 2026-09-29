# 🗺️ Mimun — Roadmap del Backend

Plan de trabajo del backend. Contexto y arquitectura en [README.md](./README.md). Roadmap general en el [README raíz](../README.md).

**Leyenda:** ⚪ pendiente · 🟡 en progreso · ✅ terminada · 🔗 requiere coordinar con Frontend

**Estado global:** 🔴 no iniciado

> Marcar los checkboxes a medida que se completan. Una fase se considera terminada solo cuando todos sus checkboxes están marcados **y** sus tests pasan.

---

## Fase B0 — Contrato y diseño ⚪

Antes de escribir código. Es lo que permite que Android y Backend avancen en paralelo.

- [ ] Confirmar el modelo de datos en `Docs/database/`.
- [ ] Escribir el contrato inicial de `mood-entries` en `Docs/api/`. 🔗
- [ ] Escribir el contrato inicial de `journal-entries` en `Docs/api/`. 🔗
- [ ] Definir el formato de error unificado en `Docs/api/`. 🔗
- [ ] Decidir el rol de `Auth/` y registrarlo en `Docs/decisions/`.
- [ ] Registrar decisiones: enum de `mood`, UUID como ID, un registro por día.

**Terminada cuando:** existe un contrato en `Docs/api/` que Android puede usar para construir su capa de datos.

---

## Fase B1 — Setup del proyecto ⚪

- [ ] Crear estructura de carpetas (`app/`, `tests/`, `alembic/`).
- [ ] `requirements.txt` con dependencias fijadas.
- [ ] `docker-compose.yml` con PostgreSQL.
- [ ] `.env.example` y carga de configuración con `pydantic-settings`.
- [ ] `main.py` con FastAPI y endpoint `GET /health`.
- [ ] Conexión a la base de datos (engine y sesión).
- [ ] Configurar `ruff` y `pytest`.
- [ ] `conftest.py` con base de datos de test.

**Terminada cuando:** `uvicorn` levanta, `/health` responde `200` y `pytest` corre (aunque sea con un test trivial).

---

## Fase B2 — Base de datos y migraciones ⚪

- [ ] Configurar Alembic.
- [ ] Enum `MoodType` (`RAD, GOOD, NORMAL, MEH, BAD, AWFUL`).
- [ ] Modelo `User`.
- [ ] Modelo `MoodEntry` con `UNIQUE (user_id, date)`.
- [ ] Modelo `JournalEntry`.
- [ ] Primera migración aplicada sobre una base vacía.
- [ ] Índices definidos en el diseño.

**Terminada cuando:** `alembic upgrade head` crea todas las tablas y `alembic downgrade base` las elimina sin errores.

---

## Fase B3 — CRUD de registros emocionales ⚪ 🔗

Se desarrolla **sin autenticación real** al principio, con un usuario fijo de desarrollo, y se protege en B6.

- [ ] Schemas Pydantic de `MoodEntry` (create, update, response).
- [ ] `MoodEntryRepository`.
- [ ] `MoodEntryService` (regla: un registro por día → `409`).
- [ ] `POST /mood-entries`.
- [ ] `GET /mood-entries` con filtros `from` y `to`.
- [ ] `GET /mood-entries/{id}`.
- [ ] `PUT /mood-entries/{id}`.
- [ ] `DELETE /mood-entries/{id}`.
- [ ] Manejo de errores con el formato unificado.
- [ ] Tests de servicio y de API (casos felices y de error).

**Terminada cuando:** el CRUD funciona según `Docs/api/` y Android puede consumirlo contra un backend local.

---

## Fase B4 — Reflexiones (Journal) ⚪ 🔗

- [ ] Schemas de `JournalEntry`.
- [ ] `JournalEntryRepository` y `JournalEntryService`.
- [ ] CRUD completo de `/journal-entries`.
- [ ] Relación opcional con `MoodEntry` (validar que pertenezca al mismo usuario).
- [ ] Paginación en el listado.
- [ ] Tests de servicio y de API.

**Terminada cuando:** se puede crear, editar, listar y borrar reflexiones, con o sin registro emocional asociado.

---

## Fase B5 — Estadísticas ⚪ 🔗

- [ ] `GET /stats/mood-distribution` (conteo y porcentaje por estado en un rango).
- [ ] Definir en `Docs/api/` el formato de la respuesta. 🔗
- [ ] Endpoint de evolución temporal (ej.: promedio o racha por semana), si Analytics lo requiere.
- [ ] Consultas agregadas eficientes (SQL, no cargar todo en memoria).
- [ ] Tests con datos de ejemplo.

**Terminada cuando:** Analytics en Android puede pedir sus estadísticas sin calcularlas localmente.

---

## Fase B6 — Autenticación ⚪ 🔗

Depende de la decisión sobre `Auth/` (B0).

- [ ] Hash de contraseñas.
- [ ] `POST /auth/register`.
- [ ] `POST /auth/login` (access + refresh token).
- [ ] `POST /auth/refresh`.
- [ ] Dependencia `get_current_user`.
- [ ] Proteger todos los endpoints de datos.
- [ ] Garantizar que cada usuario solo ve sus datos (tests de aislamiento).
- [ ] Errores `401` / `403` según el contrato.
- [ ] Documentar el flujo en `Docs/api/`. 🔗

**Terminada cuando:** ningún endpoint de datos responde sin token válido y un usuario no puede leer datos de otro.

---

## Fase B7 — Soporte de sincronización ⚪ 🔗

Para que Android pueda trabajar offline y sincronizar después.

- [ ] Aceptar IDs generados por el cliente (UUID).
- [ ] Definir estrategia de conflictos (ej.: gana `updated_at` más reciente) y documentarla. 🔗
- [ ] Endpoint o parámetro para obtener cambios desde una fecha (`updated_since`).
- [ ] Borrado lógico si la sincronización lo requiere.
- [ ] Tests de escenarios de sincronización.

**Terminada cuando:** el contrato de sincronización está documentado y probado con casos de conflicto.

---

## Fase B8 — Calidad y endurecimiento ⚪

- [ ] Cobertura razonable de tests en servicios y API.
- [ ] Validación estricta de entradas (longitudes máximas, fechas válidas).
- [ ] Rate limiting básico en `/auth`.
- [ ] Configuración de CORS.
- [ ] Logging estructurado.
- [ ] Revisión de índices con datos de prueba grandes.
- [ ] Revisión de seguridad (secretos, hashing, expiración de tokens).

---

## Fase B9 — Deploy ⚪

- [ ] `Dockerfile` del backend.
- [ ] Configuración por entorno (dev / prod).
- [ ] Elegir hosting y base de datos gestionada.
- [ ] Migraciones automáticas en el deploy.
- [ ] HTTPS.
- [ ] Backups de la base de datos.
- [ ] Documentar el proceso en `Docs/architecture/`.

---

## 🔗 Correspondencia con el roadmap raíz

| Roadmap raíz | Fases del backend |
|---|---|
| Fase 6 — Backend | B0 a B5 |
| Fase 7 — Integración (parte backend) | B6, B7 |
| Fase 8 — Testing y refinamiento | B8 |
| — (fuera del roadmap raíz) | B9 |

## 📎 Hitos que desbloquean al Frontend

| Hito | Qué habilita en Android |
|---|---|
| Fin de B0 | Construir DTOs, Retrofit y repositorios contra un contrato estable. |
| Fin de B3 | Probar el registro emocional contra una API real. |
| Fin de B4 | Integrar reflexiones. |
| Fin de B5 | Integrar Analytics con datos del servidor. |
| Fin de B6 | Implementar login y sesión en la app. |
| Fin de B7 | Implementar sincronización offline. |
