# 🚀 Levantar el backend en local (rutina diaria)

Guía para trabajar con el Backend de Mimun en tu PC y probar la app Android contra la API.

**Resumen:** la base de datos (PostgreSQL) corre en Docker y la API corre en tu terminal, con recarga automática al editar código. Se usan **dos terminales**.

> Todos los comandos son para **PowerShell**, desde la carpeta `Backend`.

---

## 0. Requisitos (se hacen una sola vez)

- **Docker Desktop** instalado.
- **Entorno virtual** creado en `Backend/.venv` con las dependencias (`pip install -r requirements.txt`).
- **Archivo `.env`** en `Backend/`. Si no existe, se crea desde la plantilla:

```powershell
cd <ruta-del-repositorio>\Backend
if (-not (Test-Path .env)) { Copy-Item .env.example .env }
```

Valores que importan para trabajar con el Frontend (Fase F6):

| Variable | Valor | Para qué |
|---|---|---|
| `AUTH_ENABLED` | `false` | La API usa un usuario fijo de desarrollo y no pide token. Pasa a `true` en F7. |
| `ENVIRONMENT` | `development` | Habilita `/docs` y las comodidades de desarrollo. |
| `DATABASE_URL` | `postgresql+psycopg://mimun:mimun@localhost:5432/mimun` | Conexión a la base de datos de Docker. |
| `SECRET_KEY` | cualquier texto largo | Firma de los tokens. En producción debe ser propia y de 32+ caracteres. |

---

## 1. Rutina diaria: arrancar

### Paso 1. Abrir Docker Desktop

Ábrelo y espera a que diga **Engine running** (o "Running"). Sin esto, los comandos `docker` no funcionan.

### Paso 2. Levantar la base de datos (Terminal 1)

```powershell
cd <ruta-del-repositorio>\Backend
docker compose up -d db
docker compose ps
```

En `docker compose ps` debe aparecer `mimun_db` con estado **`healthy`**. Si dice `starting`, espera unos segundos y repite el comando.

Los datos se guardan en un volumen de Docker, así que **se conservan** entre un día y otro.

### Paso 3. Activar el entorno virtual y aplicar migraciones

```powershell
.\.venv\Scripts\Activate.ps1
alembic upgrade head
```

El prompt debe empezar con `(.venv)`. Si la base de datos ya está al día, `alembic` no muestra ninguna migración y eso es normal. Ejecútalo siempre: tarda un segundo y evita errores cuando hay migraciones nuevas.

### Paso 4. Arrancar la API (misma Terminal 1)

```powershell
uvicorn app.main:app --reload
```

Debe terminar con:

```
Application startup complete.
Uvicorn running on http://127.0.0.1:8000
```

**Deja esta terminal abierta y sin tocar.** Aquí aparece cada petición que llega, incluidas las de la app. Si pulsas `Ctrl+C`, la API se apaga.

### Paso 5. Comprobar que responde (Terminal 2)

Abre una terminal nueva:

```powershell
cd <ruta-del-repositorio>\Backend
Invoke-RestMethod http://localhost:8000/api/v1/health
```

Debe responder `status: ok`. También puedes abrir `http://localhost:8000/docs` en el navegador (Swagger).

---

## 2. Probar la app Android contra la API

1. Con la API corriendo (Paso 4), abre el **emulador** y ejecuta la app desde Android Studio.
2. La app apunta a `http://10.0.2.2:8000/api/v1/`. Esa dirección es cómo el emulador ve el `localhost` de tu PC, y no hay que cambiarla.
3. Usa la app (cambia la emoción, agrega una nota) y **mira la Terminal 1**. Cada petición aparece ahí:

| Lo que ves en la Terminal 1 | Qué significa |
|---|---|
| `PUT /api/v1/...` con `200` o `201` | Sincronizó bien. |
| `422` | El servidor rechazó los datos (formato o campo inválido). |
| `401` | Falta el token (solo ocurre con `AUTH_ENABLED=true`). |
| `429` | Demasiados intentos en `/auth` desde la misma IP. |
| Ninguna línea nueva | La petición no llega al servidor. Revisa que la API esté corriendo. |

Para ver el detalle completo de una petición o respuesta, usa **Logcat** en Android Studio y filtra por `OkHttp`.

### Con un celular real (en vez del emulador)

1. Arranca la API aceptando conexiones de la red: `uvicorn app.main:app --reload --host 0.0.0.0`.
2. Usa la IP de tu PC en la red local (`ipconfig`) en lugar de `10.0.2.2`.
3. Permite el puerto 8000 en el firewall de Windows.
4. Agrega esa IP al archivo `network_security_config.xml` de la app, que hoy solo permite `10.0.2.2`.

---

## 3. Ver qué hay guardado en la base de datos

Desde la Terminal 2:

```powershell
docker exec -it mimun_db psql -U mimun -d mimun -c "select * from mood_entries;"
docker exec -it mimun_db psql -U mimun -d mimun -c "select * from journal_entries;"
docker exec -it mimun_db psql -U mimun -d mimun -c "select id, email from users;"
```

Entrar a una consola interactiva de PostgreSQL (se sale con `\q`):

```powershell
docker exec -it mimun_db psql -U mimun -d mimun
```

---

## 4. Rutina diaria: apagar

1. En la Terminal 1, `Ctrl+C` para detener la API.
2. Detener la base de datos (conserva los datos):

```powershell
docker compose stop db
```

Si prefieres dejarla corriendo, no pasa nada: consume pocos recursos.

---

## 5. Cuando cambias algo

| Cambio | Qué hacer |
|---|---|
| Código de Python | Nada: `--reload` reinicia la API solo. |
| Migración nueva de Alembic | `alembic upgrade head` |
| Variables del `.env` | Reiniciar `uvicorn` (`Ctrl+C` y volver a arrancar). |
| Dependencias nuevas | `pip install -r requirements.txt` (con el entorno activado). |

---

## 6. Antes de hacer un commit

Con el entorno virtual activado:

```powershell
ruff check . --fix
ruff format .
lint-imports
pytest
```

Todo debe pasar. Las pruebas de integración usan la base `mimun_test`. Si no existe, se crea con:

```powershell
docker exec -it mimun_db psql -U mimun -d mimun -c "CREATE DATABASE mimun_test;"
```

---

## 7. Alternativa: API y base de datos, ambas en Docker

```powershell
cd <ruta-del-repositorio>\Backend
docker compose up --build
```

Construye la imagen de la API, aplica las migraciones y la arranca junto con la base de datos. Se detiene con `Ctrl+C` y luego `docker compose down`.

> **No uses esta opción a la vez que `uvicorn` en tu terminal.** Los dos usan el puerto 8000.

---

## 8. Comandos útiles de Docker

```powershell
docker compose ps           # estado de los contenedores
docker compose logs -f db   # logs de la base de datos
docker compose stop db      # apagar la base de datos (conserva los datos)
docker compose down         # eliminar los contenedores (conserva los datos)
docker compose down -v      # ⚠️ BORRA también todos los datos (empezar de cero)
```

> ⚠️ `down -v` elimina el volumen con todos tus registros de desarrollo. Después hay que correr `alembic upgrade head` para recrear las tablas.

---

## 9. Problemas comunes

| Síntoma | Qué hacer |
|---|---|
| `docker` no responde o falla al conectar | Docker Desktop no está abierto o no terminó de arrancar. Espera a "Engine running". |
| `port is already allocated` (5432 u 8000) | Otro programa usa ese puerto (otro PostgreSQL, otro `uvicorn`, o la opción Docker de la API). Ciérralo o cambia `API_PORT` en el `.env`. |
| `connection refused` al correr `alembic` o `uvicorn` | La base de datos no está lista. Revisa que `docker compose ps` diga `healthy`. |
| `alembic` o `uvicorn` no se reconocen | Falta activar el entorno virtual (el prompt debe empezar con `(.venv)`). |
| La app no conecta desde el emulador | Comprueba `Invoke-RestMethod http://localhost:8000/api/v1/health` desde tu PC. Si ahí funciona, revisa que la URL de la app sea `http://10.0.2.2:8000/api/v1/`. |
| `429` al registrarte | Hay un límite de intentos por IP. Espera, o súbelo en `.env` (`AUTH_RATE_LIMIT_ATTEMPTS`). |
| `401` en los endpoints de datos | `AUTH_ENABLED=true` y la petición no lleva token válido. Para F6 déjalo en `false`. |

---

## Resumen de una línea por paso

```powershell
# Terminal 1 (se queda abierta con la API)
cd <ruta-del-repositorio>\Backend
docker compose up -d db
.\.venv\Scripts\Activate.ps1
alembic upgrade head
uvicorn app.main:app --reload

# Terminal 2 (comprobar y consultar)
Invoke-RestMethod http://localhost:8000/api/v1/health
docker exec -it mimun_db psql -U mimun -d mimun -c "select * from mood_entries;"
```
