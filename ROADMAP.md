# 🗺️ Mimun — Roadmap general

Plan de alto nivel del proyecto. Contexto en el [README raíz](./README.md). El detalle vive en cada módulo:

- 📱 [Frontend/ROADMAP.md](./Frontend/ROADMAP.md) — fases F1 a F9
- 🖥️ [Backend/ROADMAP.md](./Backend/ROADMAP.md) — fases B0 a B9

**Leyenda:** ⚪ pendiente · 🟡 en progreso · ✅ terminada

> Actualizar el estado cuando cambien los roadmaps de los módulos.

---

## Fases

| # | Fase | Estado | Frontend | Backend |
|---|---|---|---|---|
| 1 | **Foundation**: proyecto, tema, navegación, componentes base | ✅ | F1 | — |
| 2 | **Mood**: selector, personajes animados, registro del día | ✅ | F2 | — |
| 3 | **Journal**: reflexiones (Notes) | ✅ | F3 | — |
| 4 | **Persistencia local**: Room | ✅ | F4 | — |
| 5 | **Analytics**: historial, calendario y estadísticas locales | ✅ | F5 | — |
| 6 | **Backend**: contrato, base de datos, CRUD, reflexiones y estadísticas | 🟡 | — | B0 a B5 (B0 a B4 ✅, B5 opcional) |
| 7 | **Integración**: API, autenticación y sincronización | 🟡 | F6, F7 | B6 ✅, B7 |
| 8 | **Testing y refinamiento** | ⚪ | F8 | B8 |
| — | **Publicación y deploy** (fuera del roadmap original) | ⚪ | F9 | B9 |

---

## Orden de dependencias

```
Fases 1 a 5 (Frontend, 100% local)      Fase 6 (Backend)
                │                              │
                └──────────────┬───────────────┘
                               ▼
                   Fase 7 — Integración
                               ▼
                 Fase 8 — Testing y refinamiento
```

- Las fases 1 a 5 del Frontend **no dependen del backend**: usan datos en memoria o Room.
- La fase 6 del Backend **no depende del Frontend**, salvo por el contrato en [`Docs/api/`](./Docs/api/README.md).
- El **contrato de la API** (Backend B0) es lo que permite avanzar en paralelo.
- La fase 7 necesita que existan el contrato, el CRUD (B3, B4) y, para login y sync, B6 y B7.

---

## Próximos hitos

| Hito | Módulo | Qué desbloquea |
|---|---|---|
| ~~Cerrar B0 (contrato y decisiones)~~ ✅ 2026-09-29 | Backend | Android ya puede construir DTOs y Retrofit contra el contrato |
| F6: conectar Android a `mood-entries` y `journal-entries` (backend B3 y B4 listos) | Frontend | Probar la integración contra la API real |
| ~~B6: autenticación~~ ✅ 2026-09-30 | Backend | F7: login y sesión |
| B7: sincronización | Backend | F7: trabajo offline |
