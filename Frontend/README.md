# 🌿 Mimun — Tu Diario Emocional

> Una aplicación móvil minimalista y elegante diseñada para el bienestar emocional, construida 100% nativa para Android con **Kotlin** y **Jetpack Compose**.

---

## 📸 Capturas de Pantalla (*Screenshots*)

<p align="center">
  <img src="screenshots/notes_screen.png" width="30%" alt="Notas y Reflexiones" /> &nbsp;&nbsp;&nbsp;
  <img src="screenshots/mood_screen.png" width="30%" alt="Registro de Estado de Ánimo" /> &nbsp;&nbsp;&nbsp;
  <img src="screenshots/analytics_screen.png" width="30%" alt="Estadísticas y Calendario" />
</p>

*(Explora la carpeta `screenshots/` para ver en detalle cada una de las vistas de la aplicación).*

---

## ✨ ¿Qué es Mimun?

**Mimun** es mucho más que un diario personal; es un espacio seguro y visualmente acogedor para conectar con tus emociones cotidianas. En un mundo acelerado, Mimun te invita a detenerte un momento, registrar cómo te sientes, escribir tus reflexiones y visualizar tus patrones emocionales a lo largo del tiempo con gráficos intuitivos y un diseño orgánico que transmite absoluta calma.

---

## 🚀 Características Principales

### 🎨 1. Diseño Minimalista y Orgánico (*UI/UX*)
* **Paleta de Colores Cálida:** Inspirada en la naturaleza (tonos crema, beiges suaves y verdes oliva/salvia).
* **Ilustraciones Integradas:** Fondos orgánicos que acompañan al usuario sin recargar la interfaz.
* **Transiciones Fluidas:** Animaciones de fundido (*Crossfade*) y microinteracciones elásticas en cada selección.

### 😊 2. Registro Emocional Animado
* **5 Estados de Ánimo:** `RAD` (Increíble), `GOOD` (Bien), `MEH` (Indiferente), `BAD` (Mal) y `AWFUL` (Terrible).
* **Personajes Animados (*Frame-by-Frame*):** Al seleccionar un estado de ánimo, los personajes cobran vida con animaciones fluidas de 9 fotogramas.

### 📝 3. Diario de Reflexiones e Inteligencia Visual
* **Iconos Aleatorios Automáticos:** Cada nota nueva que escribes recibe automáticamente un icono decorativo único (`apple`, `sun`, `sky`, `great_v2`) generado por el sistema.
* **Vinculación con tu Emoción:** Tus notas se conectan de forma inteligente con el estado de ánimo que registraste ese mismo día, mostrando su carita correspondiente en la tarjeta.
* **Filtros Inteligentes:** Organiza tus pensamientos al instante con pastillas de filtro interactivas (*Todas*, *Hoy*, *Esta semana*, *Este mes*).
* **Vista de Detalle en Pantalla Completa:** Toca cualquier tarjeta para abrir una lectura inmersiva sin recortes, con opciones de edición y borrado.

### 📊 4. Analytics y Autoconocimiento Profundo
* **Calendario Emocional Mensual:** Un historial visual mes a mes donde cada día refleja el emoji de cómo te sentiste.
* **Gráfico de Onda (*Gusanito Emocional*):** Una curva de evolución mensual con un tierno personaje flotando exactamente en la punta del último registro.
* **Gráfico Circular de Distribución (*Donut Chart*):** Porcentajes exactos y desglose estadístico de tus estados de ánimo predominantes.

### 🔒 5. Seguridad y Sincronización (*Offline-First*)
* **Autenticación Segura (JWT):** Flujo completo de Registro, Inicio de sesión, Refresh Token y Cierre de sesión.
* **Almacenamiento Cifrado:** Uso de `EncryptedSharedPreferences` respaldado por el KeyStore del dispositivo para proteger los tokens de acceso.
* **Sincronización Bidireccional (*SyncManager*):** Algoritmo inteligente *Push & Pull* para trabajar sin conexión y sincronizar notas y emociones automáticamente con el backend.

---

## 🧰 Stack Tecnológico

| Capa / Componente | Tecnología |
|---|---|
| **Lenguaje** | Kotlin 100% |
| **Interfaz de Usuario** | Jetpack Compose + Material 3 (Sin XML) |
| **Arquitectura** | MVVM + Clean Architecture (Domain, Data, UI) |
| **Navegación** | Navigation Compose |
| **Gestión de Estado** | ViewModel + StateFlow + Coroutines |
| **Persistencia Local** | Room (SQLite con migraciones seguras) |
| **Red & API REST** | Retrofit + OkHttp + OkHttp Authenticator (401 Auto-refresh) |
| **Serialización** | kotlinx.serialization |
| **Seguridad** | EncryptedSharedPreferences & MasterKey KeyStore |
| **Testing** | JUnit 4 (Pruebas unitarias de mapeadores y repositorios) |

---

## 🗂️ Estructura del Proyecto (Clean Architecture)

```
Frontend/app/src/main/java/com/canoezequiel/moodflow/
├── data/
│   ├── local/                 # Base de datos Room, DAOs y TokenManager cifrado
│   ├── mapper/                # Mapeadores bidireccionales (Entidad/DTO ↔ Dominio)
│   ├── remote/                # Clientes Retrofit, interceptores de autenticación y SyncManager
│   └── repository/            # Implementaciones concretas de repositorios
│
├── domain/
│   ├── model/                 # Modelos de negocio puros (Mood, JournalEntry, AnalyticsSummary)
│   ├── repository/            # Interfaces de repositorio (Contratos de datos)
│   └── usecase/               # Casos de uso de negocio (Login, GetAnalytics, Guardar nota, etc.)
│
└── ui/
    ├── components/            # Componentes reutilizables (JournalCard, NoteDetailDialog, etc.)
    ├── navigation/            # Grafo de navegación global y rutas de la app
    ├── screens/               # Pantallas principales (MoodSelectionScreen, NotesScreen, AnalyticsScreen)
    ├── theme/                 # Paleta de colores Mimun, tipografías y temas Material 3
    └── viewmodel/             # ViewModels reactivos con StateFlow
```

---

## 🛠️ Configuración y Ejecución

1. Clonar el repositorio y abrir la carpeta `Frontend` en **Android Studio** (versión Jellyfish o superior).
2. Asegurarse de tener configurado el **JDK 17+**.
3. Sincronizar el proyecto con Gradle.
4. Ejecutar el módulo `app` en un emulador o dispositivo físico Android.
   * *Nota:* Para conectar con el servidor local del backend, la URL base predeterminada en el entorno es `http://10.0.2.2:8000/api/v1/`.

Comandos útiles de Gradle:
```bash
./gradlew assembleDebug        # Compilar la aplicación en modo debug
./gradlew test                 # Ejecutar la suite completa de tests unitarios
```
