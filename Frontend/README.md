# 📱 Mimun — Frontend (Android)

App Android de Mimun, hecha 100% con **Kotlin + Jetpack Compose**.

> Antes de trabajar acá, leer el [README raíz](../README.md). Este documento detalla solo el frontend. Si algo contradice al README raíz, gana el raíz.
> El plan de trabajo está en [ROADMAP.md](./ROADMAP.md). El contrato de la API vive en [`../Docs/api/`](../Docs/api/).

---

# 📍 Estado actual

🟢 **Fases F1 a F7 completadas al 100%.**

- **F1 — Foundation ✅**: Jetpack Compose, Material 3, Clean Architecture y navegación compartida (**Notes**, **Mood**, **Analytics**).
- **F2 — Mood ✅**: Selección de emociones (`RAD, GOOD, MEH, BAD, AWFUL`), personajes animados y nota diaria.
- **F3 — Journal (Notes) ✅**: Diario completo de reflexiones con lista, creación, edición y eliminación de notas.
- **F4 — Persistencia Local (Room) ✅**: Base de datos SQLite local para guardar notas y emociones permanentemente.
- **F5 — Analytics ✅**: Calendario mensual con emojis, curva de evolución emocional ("gusanito") y desglose de porcentajes.
- **F6 — Integración con la API ✅**: Integración con la API REST del Backend (`POST/GET /api/v1/mood-entries` y `journal-entries`).
- **F7 — Autenticación y Sincronización ✅**: Autenticación segura con JWT (login/registro/refresh token), cifrado seguro con `TokenManager`, `OkHttp Authenticator` y algoritmo de sincronización bidireccional (*Push & Pull*) con manejo de tombstones y resolución de conflictos.
- **📍 SIGUIENTE PASO — Fase F8**: Testing y refinamiento.

---

## 🎯 Responsabilidades

- Interfaz de usuario y navegación.
- Interacción y animaciones.
- Personajes emocionales animados.
- Persistencia local (Room).
- Consumo de la API REST (Retrofit).
- Autenticación y refresco automático de tokens JWT.
- Sincronización offline-first de emociones y reflexiones.
- Estados de carga, vacío y error.

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
| Persistencia local | Room |
| Red | Retrofit + OkHttp + OkHttp Authenticator |
| Serialización | kotlinx.serialization |
| Cifrado | EncryptedSharedPreferences (MasterKey KeyStore) |
| Tests | JUnit 4, MockK, Compose UI Test |

---

## 🗂️ Estructura de paquetes

```
Frontend/app/src/main/java/com/canoezequiel/moodflow/
├── data/
│   ├── local/                 # Room: entidades, DAOs, base de datos y TokenManager cifrado
│   │   ├── auth/              # TokenManager cifrado con EncryptedSharedPreferences
│   │   ├── dao/               # MoodEntryDao, JournalEntryDao
│   │   ├── database/          # AppDatabase (Room)
│   │   ├── entity/            # MoodEntryEntity, JournalEntryEntity
│   │   ├── remote/dto/        # AuthDtos, MoodEntryDto, JournalEntryDto, PageDto
│   │   └── sync/              # SyncPreferences
│   ├── mapper/                # Mappers (Entidad/DTO ↔ Dominio)
│   ├── remote/
│   │   ├── api/               # ApiClient (Retrofit), ApiService
│   │   ├── auth/              # AuthInterceptor, TokenAuthenticator (refresco 401)
│   │   └── sync/              # SyncManager (Algoritmo Push & Pull)
│   └── repository/            # AuthRepositoryImpl, MoodRepositoryImpl, JournalRepositoryImpl
│
├── domain/
│   ├── model/                 # Mood, MoodEntry, JournalEntry, MoodType, AnalyticsSummary
│   ├── repository/            # Interfaces AuthRepository, MoodRepository, JournalRepository
│   └── usecase/               # LoginUseCase, RegisterUseCase, GetAnalyticsUseCase, etc.
│
├── ui/
│   ├── components/            # MimunButton, MimunTextField, JournalCard, etc.
│   ├── navigation/            # RootNavGraph, Screen, MainScreen
│   ├── screens/               # SplashScreen, OnboardingScreen, LoginScreen, RegisterScreen, etc.
│   ├── theme/                 # Color, Type, Theme
│   └── viewmodel/             # AuthViewModel, MoodViewModel, NotesViewModel, AnalyticsViewModel
│
└── MainActivity.kt
```

---

## 🧱 Capas y reglas de dependencia

```
ui  →  domain  ←  data
```

| Capa | Contiene | Reglas |
|---|---|---|
| **domain** | Modelos, interfaces de repositorio, use cases. | Kotlin puro. No depende de Android, Room, Retrofit ni Compose. |
| **data** | Room, Retrofit, DTOs, mappers, repositorios, SyncManager. | Implementa las interfaces de `domain`. Nunca depende de `ui`. |
| **ui** | Screens, ViewModels, Components. | Depende de `domain`. Nunca importa clases de `data` (ni entidades ni DTOs). |

---

## 😊 Estados emocionales

Los valores son **exactamente** estos, idénticos en todas las capas del proyecto:

```kotlin
enum class MoodType { RAD, GOOD, MEH, BAD, AWFUL }
```

| Valor | Significado |
|---|---|
| `RAD` | La está pasando genial |
| `GOOD` | Se siente bien |
| `MEH` | Indiferente / sin demasiadas emociones |
| `BAD` | No se encuentra bien |
| `AWFUL` | Estado emocional muy negativo |

Escala de mejor a peor: `RAD → GOOD → MEH → BAD → AWFUL`.

---

## 🛠️ Entorno de desarrollo

- **Android Studio** (versión estable reciente) y **JDK 17+**.
- Abrir la carpeta `Frontend` y ejecutar el módulo `app`.
- Emulador o dispositivo físico con la versión mínima de SDK definida en `build.gradle`.
- Base URL en el emulador Android: `http://10.0.2.2:8000/api/v1/`.

Comandos útiles:

```bash
./gradlew assembleDebug        # compilar
./gradlew test                 # tests unitarios
```