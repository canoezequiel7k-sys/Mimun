# 🗺️ Mimun — Roadmap del Frontend (Android)

Plan de trabajo de la app Android. Contexto general en el [README raíz](../README.md). Roadmap del backend en [`../Backend/ROADMAP.md`](../Backend/ROADMAP.md).

**Leyenda:** ⚪ pendiente · 🟡 en progreso · ✅ terminada · 🔗 requiere coordinar con Backend

**Estado global:** 🟡 en progreso (estructura visual avanzada, sin persistencia ni API)

> Marcar los checkboxes a medida que se completan. Ajustar los ya marcados según lo que realmente esté hecho en el código.

---

## Fase F1 — Foundation 🟡

- [x] Proyecto Android con Jetpack Compose y Material 3.
- [x] Estructura de paquetes base (`core/`, `feature/`).
- [x] Tema visual definido (colores, tipografía, formas).
- [x] Navegación principal con barra inferior: **Notes · Mood · Analytics**.
- [x] Contenedor común de navegación (no cada pantalla por separado).
- [x] Componentes base reutilizables en `core/ui/components/`.
- [x] Inyección de dependencias configurada (Hilt).
- [x] `Frontend/README.md` con la guía de paquetes y convenciones.

**Terminada cuando:** se puede navegar entre las tres secciones y todas comparten el tema.

> Revisar y tildar según el estado real del proyecto Android.

---

## Fase F2 — Mood ⚪

Trabaja **solo con datos en memoria / fake**: no depende del backend.

- [x] Enum `Mood` en `domain` (`RAD, GOOD, NORMAL, MEH, BAD, AWFUL`), con los nombres exactos del README raíz.
- [x] Modelo `MoodEntry` en `domain`.
- [x] Interfaz `MoodRepository` en `domain`.
- [x] Implementación fake del repositorio.
- [x] Personajes circulares de los 6 estados (expresiones distintas).
- [x] Animaciones de cambio de expresión.
- [x] Selector de emociones.
- [x] Pantalla de registro diario (estado + nota corta opcional).
- [x] `MoodViewModel` con `UiState` expuesto por `StateFlow`.
- [x] Use cases: registrar estado, obtener registro del día.
- [x] Previews de Compose para cada estado.

**Terminada cuando:** el usuario elige un estado, agrega una nota y ve confirmado el registro del día.

---

## Fase F3 — Journal (Notes) ⚪

- [x] Modelo `JournalEntry` en `domain`.
- [x] Repositorio y use cases de reflexiones (fake por ahora).
- [x] Casos de uso (`GetJournalEntriesUseCase`, `SaveJournalEntryUseCase`, `DeleteJournalEntryUseCase`)
- [x] Pantalla de lista de reflexiones.
- [x] Pantalla de crear reflexión.
- [x] Pantalla de ver y editar reflexión.
- [x] Eliminar reflexión con confirmación.
- [x] Relacionar una reflexión con el registro emocional del día.
- [x] Estados vacío, cargando y error.

**Terminada cuando:** se puede crear, editar, ver y borrar reflexiones y asociarlas a un estado emocional.

---

## Fase F4 — Persistencia local (Room) ⚪

- [x] Configurar Room.
- [x] Entidades `MoodEntryEntity` y `JournalEntryEntity`.
- [x] DAOs.
- [x] `AppDatabase` y migraciones de Room.
- [x] Mappers entidad ↔ dominio.
- [x] Reemplazar repositorios fake por implementaciones con Room.
- [x] IDs en formato UUID desde el cliente. 🔗
- [x] Campo de control de sincronización (ej.: `syncStatus`, `updatedAt`) previendo la Fase F7. 🔗
- [x] Tests de DAO y de repositorio.

**Terminada cuando:** los datos persisten entre cierres de la app y la UI no cambió al reemplazar los fakes.

---

## Fase F5 — Analytics ⚪

Primero calculado localmente sobre Room; en F6 se puede migrar a las estadísticas del servidor.

- [ ] Historial emocional (lista por fecha).
- [ ] Calendario con el estado de cada día.
- [ ] Distribución de estados (conteo y porcentaje).
- [ ] Evolución temporal (semana / mes).
- [ ] Selector de rango de fechas.
- [ ] Gráficos (implementados con Compose Canvas o librería a decidir).
- [ ] Estados vacío y cargando.
- [ ] Use cases de estadísticas.

**Terminada cuando:** Analytics muestra historial, calendario y estadísticas a partir de los datos locales.

---

## Fase F6 — Integración con la API ⚪ 🔗

**Bloqueada hasta que el contrato de `Docs/api/` esté definido** (Backend B0) y, para probar de verdad, hasta que exista B3.

- [ ] Configurar Retrofit + serialización (Moshi o kotlinx.serialization).
- [ ] DTOs según el contrato, con mappers a `domain`. 🔗
- [ ] `BaseUrl` por entorno (`10.0.2.2:8000` en emulador).
- [ ] Interceptor de logging (solo debug).
- [ ] Data source remoto para `mood-entries`. 🔗
- [ ] Data source remoto para `journal-entries`. 🔗
- [ ] Manejo del error unificado de la API. 🔗
- [ ] Estados de carga y error visibles en la UI.
- [ ] Reintentos y mensajes claros ante falta de conexión.
- [ ] Analytics con estadísticas del servidor (opcional). 🔗

**Terminada cuando:** la app lee y escribe registros y reflexiones contra el backend local.

---

## Fase F7 — Autenticación y sincronización ⚪ 🔗

Depende de Backend B6 y B7.

- [ ] Pantallas de registro e inicio de sesión.
- [ ] Almacenamiento seguro de tokens (DataStore cifrado o equivalente).
- [ ] Interceptor que agrega el token y maneja el refresco.
- [ ] Cierre de sesión y limpieza de datos locales.
- [ ] Manejo de sesión expirada (`401`).
- [ ] Sincronización: subir cambios locales pendientes. 🔗
- [ ] Sincronización: descargar cambios del servidor (`updated_since`). 🔗
- [ ] Resolución de conflictos según la estrategia documentada. 🔗
- [ ] Indicador de estado de sincronización.
- [ ] Tests de sincronización.

**Terminada cuando:** un usuario puede iniciar sesión, trabajar offline y ver sus datos sincronizados.

---

## Fase F8 — Testing y refinamiento ⚪

- [ ] Tests unitarios de ViewModels y use cases.
- [ ] Tests de repositorios.
- [ ] Tests de UI con Compose.
- [ ] Tests de navegación.
- [ ] Manejo consistente de errores en toda la app.
- [ ] Performance (recomposiciones, listas grandes, animaciones).
- [ ] Accesibilidad (content descriptions, contraste, tamaños de texto, TalkBack).
- [ ] Modo oscuro.
- [ ] Textos en recursos (`strings.xml`) para facilitar traducción.

---

## Fase F9 — Publicación ⚪

- [ ] Ícono y splash.
- [ ] Configuración de release y ofuscación.
- [ ] Firma de la app.
- [ ] Ficha de Play Store.
- [ ] Política de privacidad (la app maneja datos personales sensibles).

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
