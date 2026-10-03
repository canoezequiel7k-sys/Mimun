# 🗺️ Mimun — Roadmap del Frontend (Android)

Plan de trabajo de la app Android. Contexto general en el [README raíz](../README.md). Roadmap del backend en [`../Backend/ROADMAP.md`](../Backend/ROADMAP.md).

**Leyenda:** ⚪ pendiente · 🟡 en progreso · ✅ terminada · 🔗 requiere coordinar con Backend

**Estado global:** 🟢 Fases F1 a F7 terminadas al 100%.

> Marcar los checkboxes a medida que se completan. Ajustar los ya marcados según lo que realmente esté hecho en el código.

---

## Fase F1 — Foundation ✅ terminada

- [x] Proyecto Android con Jetpack Compose y Material 3.
- [x] Estructura de paquetes base (`data/`, `domain/`, `ui/`).
- [x] Tema visual definido (colores, tipografía, formas).
- [x] Navegación principal con barra inferior: **Notes · Mood · Analytics**.
- [x] Contenedor común de navegación (`MainScreen` y `RootNavGraph`).
- [x] Componentes base reutilizables (`MimunButton`, `MimunTextField`, etc.).
- [x] `Frontend/README.md` con la guía de paquetes y convenciones.

---

## Fase F2 — Mood ✅ terminada

- [x] Enum `MoodType` en `domain` (`RAD, GOOD, MEH, BAD, AWFUL`), con los nombres exactos del contrato.
- [x] Modelo `MoodEntry` en `domain`.
- [x] Interfaz `MoodRepository` en `domain`.
- [x] Personajes circulares de los 5 estados emocionales.
- [x] Animaciones de cambio de expresión.
- [x] Selector de emociones.
- [x] Pantalla de registro diario (estado + nota corta opcional).
- [x] `MoodViewModel` con `UiState` expuesto por `StateFlow`.
- [x] Use cases: registrar estado, obtener registro del día.

---

## Fase F3 — Journal (Notes) ✅ terminada

- [x] Modelo `JournalEntry` en `domain`.
- [x] Repositorio y use cases de reflexiones (`GetJournalEntriesUseCase`, `SaveJournalEntryUseCase`, `DeleteJournalEntryUseCase`).
- [x] Pantalla de lista de reflexiones (`NotesScreen`).
- [x] Diálogo/pantalla de crear y editar reflexión.
- [x] Eliminar reflexión con confirmación.
- [x] Relacionar una reflexión con el registro emocional del día (`moodEntryId`).

---

## Fase F4 — Persistencia local (Room) ✅ terminada

- [x] Configurar Room.
- [x] Entidades `MoodEntryEntity` y `JournalEntryEntity`.
- [x] DAOs (`MoodEntryDao`, `JournalEntryDao`).
- [x] `AppDatabase` y migración oficial `1 -> 2` (`MIGRATION_1_2`).
- [x] Mappers entidad ↔ dominio.
- [x] IDs en formato UUID desde el cliente.
- [x] Campos de control de sincronización (`syncStatus`, `updatedAt`, `deletedAt`).

---

## Fase F5 — Analytics ✅ terminada

- [x] Historial emocional.
- [x] Calendario con el estado de cada día.
- [x] Distribución de estados (conteo y porcentaje).
- [x] Evolución temporal ("gusanito" / curva emocional en Compose Canvas).
- [x] Use cases de estadísticas (`GetAnalyticsUseCase`).

---

## Fase F6 — Integración con la API ✅ terminada

- [x] Configurar Retrofit + `kotlinx.serialization`.
- [x] DTOs según el contrato (`AuthDtos`, `MoodEntryDto`, `JournalEntryDto`, `PageDto`, etc.).
- [x] `BaseUrl` por entorno (`10.0.2.2:8000` en emulador).
- [x] Interceptor de logging con nivel `BASIC` y redacción de header de autorización.
- [x] Data sources y repositorios conectados a la API REST.

---

## Fase F7 — Autenticación y sincronización ✅ terminada

- [x] Pantallas de registro e inicio de sesión (`LoginScreen`, `RegisterScreen`).
- [x] Almacenamiento seguro de tokens (`TokenManager` con `EncryptedSharedPreferences`).
- [x] Interceptor de autenticación (`AuthInterceptor`) y refresco automático 401 (`TokenAuthenticator`).
- [x] Cierre de sesión (`logout`) notificando a la API y limpiando datos locales.
- [x] Manejo de sesión expirada (`401`).
- [x] Sincronización Push: subir cambios locales pendientes (`PENDING` y tombstones `PENDING_DELETE`).
- [x] Sincronización Pull: descargar cambios del servidor (`updated_since`) paginado.
- [x] Resolución de conflictos según la estrategia documentada (`409 MOOD_ENTRY_ALREADY_EXISTS`).
- [x] Gestor de sincronización centralizado (`SyncManager` y `SyncPreferences`).
- [x] Tests de mapeo y sincronización.

---

## Fase F8 — Testing y refinamiento 🟡 en progreso

- [x] Tests unitarios de mappers (`MoodMapperTest`).
- [x] Tests unitarios de repositorios y reutilización de ID (`MoodRepositoryTest`).
- [ ] Tests unitarios de ViewModels y use cases.
- [ ] Tests de UI con Compose.
- [ ] Tests de navegación.
- [ ] Accesibilidad y modo oscuro.

---

## Fase F9 — Publicación ⚪

- [ ] Ícono y splash screen nativo.
- [ ] Configuración de release y ofuscación (R8 / ProGuard).
- [ ] Firma de la app.
---

## 🔗 Correspondencia con el roadmap raíz

| Roadmap raíz | Fases del frontend |
|---|---|
| Fase 1 — Foundation | F1 |
| Fase 2 — Mood | F2 |
| Fase 3 — Journal | F3 |
| Fase 4 — Persistencia local | F4 |
| Fase 5 — Analytics | F5 |
| Fase 7 — Integración | F6, F7 |
| Fase 8 — Testing y refinamiento | F8 |
| — (fuera del roadmap raíz) | F9 |

## 📎 Dependencias con el Backend

| Fase del frontend | Necesita del backend |
|---|---|
| F1 a F5 | Nada. Se trabaja 100% local. |
| F4 (parcial) | Decisión de UUID y campos de sincronización (B0). |
| F6 | Contrato en `Docs/api/` (B0) y CRUD funcionando (B3, B4). |
| F6 (Analytics servidor) | B5. |
| F7 | B6 (auth) y B7 (sincronización). |

## 🤖 Reglas para el agente Android

1. Leer el README raíz antes de hacer cambios estructurales.
2. Trabajar solo dentro de `Frontend/Android/`. No tocar `Backend/` ni `Auth/`.
3. No inventar endpoints: si falta algo, proponerlo en `Docs/api/`.
4. Respetar las capas: `presentation → domain ← data`.
5. Mantener los estados emocionales idénticos a los definidos.
6. No asumir que existe algo que no figura en el estado actual.
7. Priorizar código simple, mantenible y comprensible.
