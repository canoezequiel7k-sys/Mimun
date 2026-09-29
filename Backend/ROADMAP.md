# 🗺️ Mimun — Roadmap del Backend

Plan de trabajo del backend. Contexto y arquitectura en [README.md](./README.md). Roadmap general en [`../ROADMAP.md`](../ROADMAP.md).

**Leyenda:** ⚪ pendiente · 🟡 en progreso · ✅ terminada · 🔗 requiere coordinar con Frontend

**Estado global:** 🟡 Fase B1 en curso (setup del proyecto). B0 cerrada.

> Marcar los checkboxes a medida que se completan. Una fase se considera terminada solo cuando todos sus checkboxes están marcados **y** sus tests pasan.

---

## Fase B0 — Contrato y diseño ✅

Antes de escribir código. Es lo que permite que Android y Backend avancen en paralelo.

- [x] Modelo de datos redactado en [`Docs/database/schema.md`](../Docs/database/schema.md).
- [x] Contrato inicial de `mood-entries` en [`Docs/api/mood-entries.md`](../Docs/api/mood-entries.md). 🔗
- [x] Contrato inicial de `journal-entries` en [`Docs/api/journal-entries.md`](../Docs/api/journal-entries.md). 🔗
- [x] Formato de error unificado en [`Docs/api/README.md`](../Docs/api/README.md). 🔗
- [x] Decisiones registradas en [`Docs/decisions/`](../Docs/decisions/README.md) (enum de `mood`, UUID, un registro por día, Clean Architecture).
- [x] Decisiones 0004 a 0007 confirmadas por el autor (2026-09-29).
- [x] Rol de `Auth/` decidido (decisión 0006): módulo dentro del backend.
- [x] Contrato revisado con el Frontend (`date` vs `timestamp`, decisión 0004). 🔗

**Terminada cuando:** las decisiones están confirmadas y Android puede construir su capa de datos con el contrato. ✅ Cumplido el 2026-09-29.

---

## Fase B1 — Setup del proyecto 🟡

- [x] Crear la estructura de capas (`domain/`, `application/`, `infrastructure/`, `presentation/`), `tests/` y `alembic/`.
- [x] `requirements.txt` y `pyproject.toml`.
- [ ] Fijar las versiones exactas de `requirements.txt` (hoy son rangos mínimos): instalar y generar el lock con `pip freeze`.
- [x] `docker-compose.yml` con PostgreSQL.
- [x] `.env.example` y carga de configuración con `pydantic-settings`.
- [x] `main.py` con FastAPI y `GET /health`.
- [x] `container.py` (composition root) conectado.
- [x] Configurar `ruff` y `pytest`.
- [x] Configurar `import-linter` con los contratos de capas.
- [x] `conftest.py` con base de datos de test.
- [ ] **Verificar en la máquina del autor**: `uvicorn`, `pytest`, `ruff check .` y `lint-imports` (ver README, sección "Instalar y correr").

**Terminada cuando:** `uvicorn` levanta, `/health` responde `200`, `pytest` corre y `lint-imports` pasa.

> Los archivos están escritos pero **todavía no se ejecutaron**: esta fase se cierra cuando los cuatro comandos de verificación pasen.

---

## Fase B2 — Dominio y persistencia ⚪

- [ ] Enum `MoodType` (`RAD, GOOD, MEH, BAD, AWFUL`) con su puntaje.
- [ ] Entidades `User`, `MoodEntry` y `JournalEntry` (dataclasses puras).
- [ ] Errores de dominio.
- [ ] Interfaces de repositorio en `domain/repositories/`.
- [ ] Configurar Alembic.
- [ ] Modelos SQLAlchemy y mappers entidad ↔ modelo.
- [ ] Primera migración aplicada sobre una base vacía (con `UNIQUE (user_id, date)` e índices).
- [ ] Implementaciones de repositorio.
- [ ] Tests de integración de los repositorios contra PostgreSQL.

**Terminada cuando:** `alembic upgrade head` crea todas las tablas, `alembic downgrade base` las elimina, y los repositorios pasan sus tests.

---

## Fase B3 — CRUD de registros emocionales ⚪ 🔗

Se desarrolla **sin autenticación real** al principio, con un usuario fijo de desarrollo, y se protege en B6.

- [ ] Commands, results y schemas Pydantic de `MoodEntry`.
- [ ] Casos de uso: crear, obtener, listar (filtros `from` / `to`), actualizar y borrar.
- [ ] Regla de negocio: un registro por día → `MoodEntryAlreadyExistsError` → `409`.
- [ ] Routers de `/mood-entries`.
- [ ] Traducción de errores de dominio al formato unificado.
- [ ] Tests de casos de uso (repos fake) y de API (casos felices y de error).

**Terminada cuando:** el CRUD funciona según `Docs/api/` y Android puede consumirlo contra un backend local.

---

## Fase B4 — Reflexiones (Journal) ⚪ 🔗

- [ ] Commands, results y schemas de `JournalEntry`.
- [ ] Casos de uso y CRUD completo de `/journal-entries`.
- [ ] Relación opcional con `MoodEntry` (validar que pertenezca al mismo usuario).
- [ ] Paginación en el listado.
- [ ] Tests de casos de uso y de API.

**Terminada cuando:** se puede crear, editar, listar y borrar reflexiones, con o sin registro emocional asociado.

---

## Fase B5 — Estadísticas ⚪ 🔗

- [ ] Definir en `Docs/api/` el formato de `GET /stats/mood-distribution`. 🔗
- [ ] Caso de uso y endpoint de distribución (conteo y porcentaje por estado en un rango).
- [ ] Endpoint de evolución temporal (ej.: promedio o racha por semana), si Analytics lo requiere.
- [ ] Consultas agregadas eficientes (SQL, no cargar todo en memoria).
- [ ] Tests con datos de ejemplo.

**Terminada cuando:** Analytics en Android puede pedir sus estadísticas sin calcularlas localmente.

---

## Fase B6 — Autenticación ⚪ 🔗

Depende de la decisión sobre `Auth/` (B0).

- [ ] Ports `PasswordHasher` y `TokenProvider` + implementaciones (argon2/bcrypt, JWT).
- [ ] Casos de uso: registrar, iniciar sesión, refrescar.
- [ ] `POST /auth/register`, `POST /auth/login`, `POST /auth/refresh`.
- [ ] Dependencia `get_current_user`.
- [ ] Proteger todos los endpoints de datos.
- [ ] Tests de aislamiento: un usuario no ve datos de otro.
- [ ] Errores `401` / `403` según el contrato.
- [ ] Documentar el flujo en `Docs/api/`. 🔗

**Terminada cuando:** ningún endpoint de datos responde sin token válido y un usuario no puede leer datos de otro.

---

## Fase B7 — Soporte de sincronización ⚪ 🔗

Para que Android pueda trabajar offline y sincronizar después.

- [ ] Aceptar IDs generados por el cliente (UUID).
- [ ] Definir la idempotencia de `POST` ante reintentos y documentarla. 🔗
- [ ] Definir estrategia de conflictos (ej.: gana `updated_at` más reciente) y documentarla. 🔗
- [ ] Endpoint o parámetro para obtener cambios desde una fecha (`updated_since`).
- [ ] Borrado lógico (`deleted_at`) si la sincronización lo requiere.
- [ ] Tests de escenarios de sincronización.

**Terminada cuando:** el contrato de sincronización está documentado y probado con casos de conflicto.

---

## Fase B8 — Calidad y endurecimiento ⚪

- [ ] Cobertura razonable de tests en casos de uso y API.
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

## 🔗 Correspondencia con el roadmap general

| Roadmap general | Fases del backend |
|---|---|
| Fase 6 — Backend | B0 a B5 |
| Fase 7 — Integración (parte backend) | B6, B7 |
| Fase 8 — Testing y refinamiento | B8 |
| — (fuera del roadmap general) | B9 |

## 📎 Hitos que desbloquean al Frontend

| Hito | Qué habilita en Android |
|---|---|
| Fin de B0 | Construir DTOs, Retrofit y repositorios contra un contrato estable. |
| Fin de B3 | Probar el registro emocional contra una API real. |
| Fin de B4 | Integrar reflexiones. |
| Fin de B5 | Integrar Analytics con datos del servidor. |
| Fin de B6 | Implementar login y sesión en la app. |
| Fin de B7 | Implementar sincronización offline. |
