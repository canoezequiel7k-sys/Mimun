# 📱 Mimun — Frontend (Android)

App Android de Mimun, hecha 100% con **Kotlin + Jetpack Compose**.

> Antes de trabajar acá, leer el [README raíz](../README.md). Este documento detalla solo el frontend. Si algo contradice al README raíz, gana el raíz.
> El plan de trabajo está en [ROADMAP.md](./ROADMAP.md). El contrato de la API vive en [`../Docs/api/`](../Docs/api/).

---

# 📍 Estado actual

🟢 **Fases F1 a F5 completadas al 100% (Cliente Local Completado).**

- **F1 — Foundation ✅**: Jetpack Compose, Material 3, Clean Architecture y navegación compartida (**Notes**, **Mood**, **Analytics**).
- **F2 — Mood ✅**: Selección de emociones (`RAD, GOOD, NORMAL, MEH, BAD, AWFUL`), personajes animados y nota diaria.
- **F3 — Journal (Notes) ✅**: Diario completo de reflexiones con lista, creación, edición y eliminación de notas.
- **F4 — Persistencia Local (Room) ✅**: Base de datos SQLite local para guardar notas y emociones permanentemente.
- **F5 — Analytics ✅**: Calendario mensual con emojis, curva de evolución emocional ("gusanito") y desglose de porcentajes.
- **📍 SIGUIENTE PASO — Fase F6**: Integración con la API REST del Backend (`POST/GET /api/v1/mood-entries` y `journal-entries`).

> Mientras no haya integración directa con la API, la app opera con repositorios fake/locales en memoria detrás de interfaces de la c

## 🎯 Responsabilidades

- Interfaz de usuario y navegación.
- Interacción y animaciones.
- Personajes emocionales animados.
- Persistencia local (Room).
- Consumo de la API REST (Retrofit).
- Estados de carga, vacío y error.

**Fuera de alcance:** lógica del servidor, acceso directo a PostgreSQL, definición del contrato de la API (se propone en `Docs/api/`, no se asume).

---

## 🧰 Stack

| Pieza | Tecnología |
|---|---|
| Lenguaje | Kotlin |
| UI | Jetpack Compose (sin XML) + Material 3 |
| Arquitectura | MVVM + Clean Architecture |
| Navegación | Navigation Compose |
| Estado | ViewModel + StateFlow |
| Asincronía | Coroutines + Flow |
| Inyección de dependencias | Hilt |
| Persistencia local | Room |
| Red | Retrofit + OkHttp |
| Serialización | Moshi o kotlinx.serialization (a decidir) |
| Tests | JUnit, MockK, Turbine, Compose UI Test |

---

## 🗂️ Estructura de paquetes

```
Frontend/Android/app/src/main/java/com/mimun/
├── core/
│   ├── navigation/            # NavHost, rutas, barra inferior
│   ├── ui/
│   │   ├── components/        # Componentes Compose reutilizables
│   │   └── theme/             # Color, Type, Shape, Theme
│   └── utils/
│
├── feature/
│   └── mood/
│       ├── journal/           # Notes: reflexiones
│       └── analytics/         # Historial, calendario, estadísticas
│
└── MainActivity.kt
```

Cada feature se organiza en tres capas:

```
feature/<nombre>/
├── data/
│   ├── local/                 # Room: entidades, DAOs
│   ├── remote/                # Retrofit: API, DTOs
│   ├── mapper/                # Entidad/DTO ↔ dominio
│   └── repository/            # Implementaciones de repositorio
├── domain/
│   ├── model/                 # Mood, MoodEntry, JournalEntry
│   ├── repository/            # Interfaces de repositorio
│   └── usecase/               # Casos de uso
└── presentation/
    ├── screen/                # Pantallas Compose
    ├── component/             # Componentes propios de la feature
    ├── viewmodel/             # ViewModels
    └── state/                 # UiState, eventos
```

> La organización interna de cada feature puede ajustarse al código existente. Lo que **no** se negocia son las reglas de dependencia de abajo.

---

## 🧱 Capas y reglas de dependencia

```
presentation  →  domain  ←  data
```

| Capa | Contiene | Reglas |
|---|---|---|
| **domain** | Modelos, interfaces de repositorio, use cases. | Kotlin puro. No depende de Android, Room, Retrofit ni Compose. |
| **data** | Room, Retrofit, DTOs, mappers, repositorios. | Implementa las interfaces de `domain`. Nunca depende de `presentation`. |
| **presentation** | Screens, ViewModels, UiState. | Depende de `domain`. Nunca importa clases de `data` (ni entidades ni DTOs). |

Reglas adicionales:

- Los **DTOs** de la API y las **entidades** de Room viven en `data` y se convierten a modelos de `domain` con mappers.
- Un **ViewModel** expone un único `UiState` por pantalla mediante `StateFlow` y recibe eventos de la UI.
- Los **Composables** no contienen lógica de negocio ni llaman a repositorios directamente.
- La UI observa el estado con `collectAsStateWithLifecycle()`.
- Los Composables reciben datos y lambdas; el estado se eleva (*state hoisting*).

---

## 😊 Estados emocionales

Los valores son **exactamente** estos, idénticos en todas las capas del proyecto:

```kotlin
enum class Mood { RAD, GOOD, NORMAL, MEH, BAD, AWFUL }
```

| Valor | Significado |
|---|---|
| `RAD` | La está pasando genial |
| `GOOD` | Se siente bien |
| `NORMAL` | Estado equilibrado |
| `MEH` | Indiferente / sin demasiadas emociones |
| `BAD` | No se encuentra bien |
| `AWFUL` | Estado emocional muy negativo |

Escala de mejor a peor: `RAD → GOOD → NORMAL → MEH → BAD → AWFUL`.

- El enum vive en `domain`. Los textos que ve el usuario (por ejemplo "Genial", "Bien") van en `strings.xml`, **no** en el enum.
- No se agregan, renombran ni traducen valores sin actualizar el README raíz, la API y el backend.

---

## 🎨 Identidad visual

Personajes circulares 2D animados:

- Formas simples, círculo perfecto, tamaño consistente.
- Una expresión facial distinta por estado.
- Animaciones basadas sobre todo en cambios de expresión.
- Sin elementos visuales innecesarios.

Guías de implementación:

- Cada personaje es un Composable (por ejemplo `MoodCharacter(mood: Mood, ...)`) dibujado con `Canvas` o formas de Compose.
- Los colores y formas salen del tema (`core/ui/theme/`), no se hardcodean en cada pantalla.
- Toda animación debe poder previsualizarse en Compose Preview.
- Respetar la preferencia de accesibilidad de reducir animaciones cuando sea posible.

---

## 🧭 Navegación

Tres secciones en una barra inferior, gestionada desde un **contenedor común** y no desde cada pantalla:

| Sección | Ruta / feature | Función |
|---|---|---|
| 📝 Notes | `feature/mood/journal` | Diario y reflexiones. |
| 😊 Mood | `feature/mood` | Selección y registro del estado emocional. |
| 📊 Analytics | `feature/mood/analytics` | Historial, calendario y estadísticas. |

Reglas:

- Las rutas se definen en `core/navigation/` como constantes o clases selladas, no como strings sueltos.
- Las pantallas reciben callbacks de navegación; **no** reciben el `NavController`.

---

## 🔌 Integración con la API

> Se activa en la Fase F6 del [ROADMAP](./ROADMAP.md). Hasta entonces se usa el repositorio fake / Room.

- El contrato oficial está en [`../Docs/api/`](../Docs/api/). **No se inventan endpoints.** Si falta algo, se propone ahí primero.
- Base URL en el emulador Android: `http://10.0.2.2:8000/api/v1`.
- Los IDs de registros son **UUID generados en el cliente**, para poder crear datos offline y sincronizarlos.
- Los errores de la API llegan con el formato unificado:

```json
{ "error": { "code": "VALIDATION_ERROR", "message": "Descripción legible", "details": [] } }
```

- Los errores se traducen a un tipo del dominio (por ejemplo `sealed class AppError`) y la UI muestra mensajes claros, sin exponer excepciones crudas.
- Para desarrollo local con HTTP (sin HTTPS) se necesita una configuración de red permitida **solo en debug**. Nunca en release.

---

## 🛠️ Entorno de desarrollo

- **Android Studio** (versión estable reciente) y **JDK 17+**.
- Abrir la carpeta `Frontend/Android` y ejecutar el módulo `app`.
- Emulador o dispositivo físico con la versión mínima de SDK definida en `build.gradle`.
- Para probar contra el backend local: levantar el backend (ver [`../Backend/README.md`](../Backend/README.md)) y usar `10.0.2.2:8000` en el emulador. En un dispositivo físico, usar la IP de la computadora en la red local.

Comandos útiles:

```bash
./gradlew assembleDebug        # compilar
./gradlew test                 # tests unitarios
./gradlew connectedAndroidTest # tests instrumentados
./gradlew lint                 # análisis estático
```

---

## ✅ Convenciones de código

- Nombres de Composables en `PascalCase`; funciones y variables en `camelCase`.
- Una pantalla por archivo: `MoodScreen.kt`, `MoodViewModel.kt`, `MoodUiState.kt`.
- Todo Composable de pantalla tiene su `@Preview`.
- Textos visibles en `strings.xml`, nunca hardcodeados.
- Sin lógica pesada dentro de Composables.
- Preferir `data class` inmutables para `UiState`.
- Commits pequeños: `feat(android): ...`, `fix(android): ...`, `refactor(android): ...`.

---

## 🧪 Testing

| Qué | Cómo |
|---|---|
| Use cases y ViewModels | JUnit + MockK + Turbine (para `Flow`). |
| Repositorios y DAOs | Tests unitarios y de Room en memoria. |
| Pantallas | Compose UI Test. |
| Navegación | Tests de navegación con `TestNavHostController`. |

Todo ViewModel y use case nuevo debería incluir sus tests.

---

## 🤖 Reglas para el agente Android

1. Leer el README raíz y este README antes de hacer cambios estructurales.
2. Trabajar solo dentro de `Frontend/Android/`. No tocar `Backend/` ni `Auth/`.
3. No inventar endpoints: si falta algo, proponerlo en `Docs/api/`.
4. Respetar las capas: `presentation → domain ← data`.
5. Mantener los estados emocionales idénticos a los definidos.
6. No asumir que existe algo que no figura en "Estado actual" o en el código.
7. Mientras no haya API, usar repositorios fake tras interfaces del dominio.
8. Toda la UI es Jetpack Compose; el estado se expone con `StateFlow`.
9. Priorizar código simple, mantenible y comprensible.
