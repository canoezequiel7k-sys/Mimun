# 🫧 Mimun — Tu diario emocional

App Android para registrar cómo te sentís cada día, escribir reflexiones y ver la evolución de tu estado emocional. Con personajes circulares animados, una interfaz simple y un backend propio que guarda y sincroniza los datos.

> Este es el **README raíz**. Define lo que es común a todo el proyecto. **Si algún README de módulo lo contradice, gana este.**
> Plan general en [ROADMAP.md](./ROADMAP.md).

---

## 📍 Estado actual

| Módulo | Estado | Detalle |
|---|---|---|
| 📱 [Frontend](./Frontend/README.md) | 🟡 En progreso | F1 a F5 terminadas (Room incluido). Siguiente: integración con la API (F6). |
| 🖥️ [Backend](./Backend/README.md) | 🟡 En progreso | B0 a B4 y B6 a B8 terminadas: API de `mood-entries` y `journal-entries`, autenticación, sincronización y endurecimiento. 129 tests en verde. B9 (deploy) en progreso. |
| 📚 [Docs](./Docs/) | 🟡 En progreso | Contrato de la API, esquema de base de datos y decisiones. |
| 🔐 Auth | ✅ Hecho | Módulo dentro del backend, no un servicio aparte ([decisión 0006](./Docs/decisions/README.md)). Implementado en B6 ([decisión 0008](./Docs/decisions/README.md)). La carpeta `Auth/` se eliminó. |

> Actualizar esta tabla cuando cambie el estado real.

---

## 🗂️ Estructura del repositorio

```
Mimun --- Tu diario Emocional/
├── README.md          ← este documento
├── ROADMAP.md         ← plan general
├── Frontend/          ← app Android (Kotlin + Jetpack Compose)
├── Backend/           ← API REST (Python + FastAPI + PostgreSQL)
└── Docs/
    ├── api/           ← contrato de la API (fuente de verdad)
    ├── database/      ← esquema de base de datos
    └── decisions/     ← registro de decisiones
```

---

## 🎯 Funcionalidades

Tres secciones en la barra inferior de la app:

| Sección | Función |
|---|---|
| 📝 **Notes** | Diario y reflexiones. |
| 😊 **Mood** | Selección y registro del estado emocional del día. |
| 📊 **Analytics** | Historial, calendario y estadísticas. |

---

## 😊 Estados emocionales

Son **exactamente** estos, en mayúsculas y en inglés, idénticos en Android, API, base de datos y tests:

`RAD`, `GOOD`, `MEH`, `BAD`, `AWFUL`

| Valor | Significado |
|---|---|
| `RAD` | La está pasando genial |
| `GOOD` | Se siente bien |
| `MEH` | Indiferente / sin demasiadas emociones |
| `BAD` | No se encuentra bien |
| `AWFUL` | Estado emocional muy negativo |

Escala de mejor a peor: `RAD → GOOD → MEH → BAD → AWFUL`.

- `NORMAL` **no existe**: fue eliminado por decisión del autor ([decisión 0002](./Docs/decisions/README.md)).
- Los textos que ve el usuario (por ejemplo "Genial", "Bien") son responsabilidad del Frontend, no del enum.
- No se agregan, renombran ni traducen valores sin actualizar este README, `Docs/api/`, el Frontend y el Backend.

---

## 🧱 Arquitectura

Ambos lados siguen **Clean Architecture**: las dependencias apuntan siempre hacia el dominio.

| Módulo | Regla |
|---|---|
| Android | `presentation → domain ← data` |
| Backend | `presentation → application → domain ← infrastructure` |

El **dominio** de cada lado es código puro: sin frameworks, sin base de datos y sin UI.

### Cómo se conectan

```
📱 Android  ──HTTP/JSON──▶  🖥️ API REST (/api/v1)  ──▶  🗄️ PostgreSQL
```

- El contrato vive en [`Docs/api/`](./Docs/api/README.md). **Ni el Frontend ni el Backend inventan endpoints**: si falta algo, se propone en `Docs/api/` primero.
- Los IDs son **UUID generados en el cliente**, para poder crear datos offline y sincronizarlos.
- Un usuario tiene **un registro emocional por día**.
- Cada usuario solo accede a sus propios datos.
- Los errores de la API usan un formato unificado.

---

## 🧰 Stack

| Módulo | Tecnología |
|---|---|
| Frontend | Kotlin, Jetpack Compose, Material 3, Hilt, Room, Retrofit |
| Backend | Python 3.12+, FastAPI, SQLAlchemy 2.x, Alembic, PostgreSQL |
| Comunicación | REST + JSON |

Detalle en el README de cada módulo.

---

## 🔒 Privacidad y seguridad

La app maneja **datos personales sensibles** (estados emocionales y reflexiones).

- Nada de secretos en el código ni en git. `.env` nunca se versiona; solo `.env.example`.
- Contraseñas siempre hasheadas. Nunca en texto plano.
- No se registran (logs) los contenidos de notas ni reflexiones.
- Se necesita una política de privacidad antes de publicar.

---

## 🤝 Reglas de trabajo

1. Antes de cambios estructurales, leer este README y el del módulo.
2. Cada módulo trabaja solo en su carpeta. Lo compartido (`Docs/`) se cambia con cuidado y avisando a la otra parte.
3. **El contrato primero:** cualquier cambio en la API se documenta en `Docs/api/` antes de implementarse.
4. Todo cambio de esquema incluye migración y actualización de `Docs/database/`.
5. Las decisiones importantes se registran en `Docs/decisions/`.
6. No asumir que algo existe: verificar el código y el "Estado actual" de cada README.
7. Commits pequeños con prefijo por módulo: `feat(android): …`, `feat(backend): …`, `docs: …`.
8. Priorizar código simple, mantenible y comprensible.

---

## 📝 Notas sobre la documentación

- El `Frontend/README.md` describe una ruta `Frontend/Android/app/.../com/mimun/`. La ruta real hoy es `Frontend/app/src/main/java/com/canoezequiel/moodflow/`.
- El `Frontend/README.md` todavía lista `NORMAL` como estado emocional. Debe corregirse.
- El `Frontend/ROADMAP.md` marca como hechos la inyección de dependencias con Hilt (F1) y los tests de DAO y de repositorio (F4), pero en el código no hay Hilt en `build.gradle.kts` y solo existen los tests de ejemplo de Android Studio. Hay que corregir el roadmap del Frontend o completar esos puntos.
