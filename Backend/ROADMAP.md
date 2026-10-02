# 🗺️ Mimun — Roadmap del Backend

Plan de trabajo del backend. Contexto y arquitectura en [README.md](./README.md). Roadmap general en [`../ROADMAP.md`](../ROADMAP.md).

**Leyenda:** ⚪ pendiente · 🟡 en progreso · ✅ terminada · 🔗 requiere coordinar con Frontend

**Estado global:** 🟡 B0 a B4 y B6 a B8 terminadas (CRUD, autenticación, sincronización y endurecimiento; 129 tests en verde). **B9 (deploy) en progreso**. B5 es opcional.

> Una fase se considera terminada solo cuando todos sus checkboxes están marcados **y** sus tests pasan.

---

## Fase B0 — Contrato y diseño ✅

- [x] Modelo de datos en [`Docs/database/schema.md`](../Docs/database/schema.md).
- [x] Contrato de `mood-entries` y `journal-entries` en [`Docs/api/`](../Docs/api/README.md). 🔗
- [x] Formato de error unificado. 🔗
- [x] Decisiones registradas en [`Docs/decisions/`](../Docs/decisions/README.md).
- [x] Decisiones 0004 a 0007 confirmadas y rol de `Auth/` decidido (módulo del backend).

---

## Fase B1 — Setup del proyecto ✅

- [x] Estructura de capas, `tests/` y `alembic/`.
- [x] `requirements.txt` con versiones fijadas y `pyproject.toml`.
- [x] `docker-compose.yml` con PostgreSQL y `.env.example`.
- [x] Configuración con `pydantic-settings`.
- [x] `main.py` con FastAPI y `GET /health`.
- [x] `container.py` (composition root).
- [x] `ruff`, `pytest` e `import-linter` configurados.
- [x] `conftest.py` con base de datos de test (`mimun_test`).

---

## Fase B2 — Dominio y persistencia ✅

- [x] `MoodType` (`RAD, GOOD, MEH, BAD, AWFUL`) con su puntaje.
- [x] Entidades `User`, `MoodEntry` y `JournalEntry`.
- [x] Errores de dominio y validación de textos.
- [x] Interfaces de repositorio en `domain/repositories/`.
- [x] Alembic configurado y migración `0001` (con `downgrade` probado).
- [x] Modelos SQLAlchemy y repositorios.
- [x] Tests de integración de los repositorios contra PostgreSQL.

---

## Fase B3 — CRUD de registros emocionales ✅ 🔗

- [x] Casos de uso: crear, obtener, listar (`from` / `to` / paginación), actualizar y borrar.
- [x] Regla de negocio: un registro por día → `409 MOOD_ENTRY_ALREADY_EXISTS`.
- [x] Routers de `/mood-entries` y schemas.
- [x] Traducción de errores al formato unificado.
- [x] Tests de casos de uso (repos fake) y de API.
- [ ] Probado desde la app Android (Fase F6 del Frontend). 🔗

Sin autenticación real: usa un usuario fijo de desarrollo hasta que se active B6.

---

## Fase B4 — Reflexiones (Journal) ✅ 🔗

- [x] Casos de uso y CRUD completo de `/journal-entries`.
- [x] Relación opcional con `MoodEntry` (se valida que sea del mismo usuario).
- [x] Paginación y filtro por `mood_entry_id`.
- [x] Tests de casos de uso y de API.
- [ ] Probado desde la app Android (Fase F6 del Frontend). 🔗

---

## Fase B6 — Autenticación ✅ 🔗

Contrato en [`Docs/api/auth.md`](../Docs/api/auth.md). Se hace antes que B5 porque el Frontend (F7) depende de ella.

- [x] Contrato documentado en `Docs/api/auth.md`. 🔗
- [x] Ports `PasswordHasher` y `TokenService` + implementaciones (argon2, JWT).
- [x] Migración `0002`: tabla `refresh_tokens`.
- [x] Casos de uso: registrar, iniciar sesión, refrescar (con rotación) y cerrar sesión.
- [x] `POST /auth/register`, `/auth/login`, `/auth/refresh`, `/auth/logout`.
- [x] `get_current_user_id` con `Authorization: Bearer`; `AUTH_ENABLED` (obligatorio en producción).
- [x] Tests de casos de uso, de API y de protección de endpoints.
- [x] Tests de aislamiento: un usuario no ve datos de otro.
- [x] Probado a mano: con `AUTH_ENABLED=true`, `GET /mood-entries` responde `401` sin token o con token inválido y `200` con token válido.

**Terminada cuando:** con `AUTH_ENABLED=true` ningún endpoint de datos responde sin token válido. ✅ Cumplido (2026-09-30).

Mientras se trabaja la Fase F6 del Frontend, `AUTH_ENABLED=false` en el `.env` local. Pasa a `true` en F7.

---

## Fase B5 — Estadísticas ⚪ (opcional) 🔗

Analytics ya funciona en Android sobre Room. Solo hace falta si se quiere calcular en el servidor.

- [ ] Definir en `Docs/api/` el formato de `GET /stats/mood-distribution`. 🔗
- [ ] Caso de uso y endpoint de distribución (conteo y porcentaje por estado en un rango).
- [ ] Endpoint de evolución temporal, si Analytics lo requiere.
- [ ] Consultas agregadas en SQL, no en memoria.
- [ ] Tests con datos de ejemplo.

---

## Fase B7 — Soporte de sincronización ✅ 🔗

Para que Android trabaje offline y sincronice después. Contrato en [`Docs/api/sync.md`](../Docs/api/sync.md). Migración `0003`.

- [x] Aceptar IDs generados por el cliente (UUID).
- [x] Definir la idempotencia de `POST` ante reintentos y documentarla. 🔗
- [x] Definir la estrategia de conflictos y documentarla. 🔗
- [x] Parámetro para obtener cambios desde una fecha (`updated_since`).
- [x] Borrado lógico (`deleted_at`).
- [x] Tests de escenarios de sincronización (conflictos, idempotencia, borrado lógico, revivir y `updated_since`).

---

## Fase B8 — Calidad y endurecimiento ✅

- [x] Cobertura razonable de tests en casos de uso y API.
- [x] Rate limiting básico en `/auth`.
- [x] `login`: igualar el tiempo de respuesta cuando el email no existe (hoy no se calcula ningún hash Argon2, y por tiempo se puede deducir qué emails están registrados).
- [x] `refresh`: detectar la reutilización de un refresh token ya revocado y cerrar todas las sesiones de ese usuario.
- [x] Test de aislamiento de `journal-entries` a nivel de repositorio y de API con dos tokens reales.
- [x] Configuración de CORS.
- [x] Logging estructurado (sin contenido de notas ni reflexiones).
- [x] Revisión de índices con datos de prueba grandes.
- [x] Revisión de seguridad (secretos, hashing, expiración y revocación de tokens).
- [x] Resolver la advertencia de deprecación de `httpx` en el `TestClient` de Starlette.

---

## Fase B9 — Deploy 🟡

- [x] `Dockerfile` del backend.
- [x] Configuración por entorno (dev / prod).
- [ ] Hosting y base de datos gestionada.
- [x] Migraciones automáticas en el deploy.
- [ ] HTTPS y backups de la base de datos.
- [x] Documentar el proceso en `Docs/architecture/`.

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
| ✅ Fin de B0 | DTOs, Retrofit y repositorios contra un contrato estable. |
| ✅ Fin de B3 y B4 | Probar F6 (mood y journal) contra una API real. |
| ✅ Fin de B6 | F7: login, sesión y tokens. |
| ✅ Fin de B7 | F7: sincronización offline. |
| Fin de B5 (opcional) | Analytics con datos del servidor. |
