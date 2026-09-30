# 🔐 API — Autenticación (`auth`)

Convenciones generales, errores y paginación en [README.md](./README.md).

**Estado:** 🟡 Borrador v1 (Fase B6).

---

## Modelo de sesión

| Token | Formato | Vida | Uso |
|---|---|---|---|
| `access_token` | JWT (HS256) con `sub` = id del usuario | `ACCESS_TOKEN_EXPIRE_MINUTES` (30 por defecto) | Header `Authorization: Bearer <access_token>` en los endpoints de datos. |
| `refresh_token` | Texto opaco aleatorio. El servidor guarda solo su hash. | `REFRESH_TOKEN_EXPIRE_DAYS` (14 por defecto) | Se cambia por un par nuevo en `POST /auth/refresh`. |

- **Rotación:** cada `refresh` revoca el refresh token usado y entrega uno nuevo. Un refresh token ya usado o revocado responde `401`.
- El cliente guarda ambos tokens de forma segura y nunca los registra en logs.

### Endpoints de datos protegidos

`mood-entries`, `journal-entries` y `stats` exigen `Authorization: Bearer <access_token>`. Si falta, es inválido o venció: `401 UNAUTHORIZED` con header `WWW-Authenticate: Bearer`. El cliente responde con un `refresh` y reintenta una vez; si también falla, cierra sesión.

`/health` y `/auth/*` son públicos.

### Modo desarrollo

Con `AUTH_ENABLED=false` (solo fuera de producción) los endpoints de datos no piden token y usan un usuario fijo de desarrollo. **Con `AUTH_ENABLED=true` no cambia nada del contrato de datos.** En producción es obligatorio `true`.

---

## Respuestas de sesión

```json
{
  "user": { "id": "3f2b8c1e-6a4d-4e0b-9c55-1d2a7e9f0b11", "email": "ana@example.com" },
  "access_token": "eyJhbGciOi...",
  "refresh_token": "kX3...",
  "token_type": "bearer",
  "expires_in": 1800
}
```

`expires_in` son los segundos de vida del access token. `POST /auth/refresh` devuelve lo mismo **sin** el campo `user`.

---

## `POST /auth/register`

**Request**

```json
{ "email": "ana@example.com", "password": "una-clave-larga" }
```

| Campo | Reglas |
|---|---|
| `email` | Formato `algo@dominio.tld`, hasta 254 caracteres. Se guarda en minúsculas y sin espacios en los extremos. Único sin distinguir mayúsculas. |
| `password` | De 8 a 128 caracteres. |

| HTTP | Cuándo |
|---|---|
| `201` | Usuario creado. Devuelve la respuesta de sesión (queda con sesión iniciada). |
| `409` `EMAIL_ALREADY_REGISTERED` | Ya existe una cuenta con ese email. |
| `422` `VALIDATION_ERROR` | Email o contraseña inválidos. |

---

## `POST /auth/login`

**Request:** igual que `register`.

| HTTP | Cuándo |
|---|---|
| `200` | Devuelve la respuesta de sesión. |
| `401` `UNAUTHORIZED` | Email o contraseña incorrectos. El mensaje no distingue cuál. |

---

## `POST /auth/refresh`

**Request**

```json
{ "refresh_token": "kX3..." }
```

| HTTP | Cuándo |
|---|---|
| `200` | Devuelve `access_token`, `refresh_token`, `token_type` y `expires_in`. El refresh token enviado queda revocado. |
| `401` `UNAUTHORIZED` | Token desconocido, vencido o ya usado. |

---

## `POST /auth/logout`

**Request:** `{ "refresh_token": "kX3..." }`

| HTTP | Cuándo |
|---|---|
| `204` | Sesión cerrada. Es idempotente: un token desconocido o ya revocado también responde `204`. |

El access token sigue siendo válido hasta que venza (vida corta). El cliente debe descartarlo.
