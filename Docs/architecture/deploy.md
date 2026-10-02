# Despliegue del backend

Para mostrar el proyecto sin coste: un Web Service Free de Render y PostgreSQL Free de Neon. Es una configuración de demo, no para guardar datos reales. Consulta los [límites actuales de Render](https://render.com/docs/free) y [precios de Neon](https://neon.com/pricing), que pueden cambiar.

## Límites del plan gratuito

- Render duerme el servicio tras 15 minutos sin tráfico. La primera petición puede tardar cerca de un minuto. Render gestiona HTTPS.
- Neon Free no caduca y no requiere tarjeta, pero limita cada proyecto a 1 GB y suspende la base tras cinco minutos inactiva. No incluye backups programados.
- Los servicios pueden reiniciarse o suspenderse al alcanzar sus límites. No uses datos reales del diario en esta demo pública.

## Despliegue

1. Crea un proyecto Neon Free y guarda su URL PostgreSQL de forma privada.
2. En Render, crea un Blueprint desde el repositorio y usa `render.yaml` en la raíz. El Blueprint configura el Docker runtime y el plan Free; solicita `DATABASE_URL` al crear el servicio y genera `SECRET_KEY`.
3. Para garantizar coste cero, no añadas método de pago. Si Render lo exige para activar el servicio, detente antes de aceptar.
4. Configura o confirma estas variables en Render:

   | Variable | Valor |
   |---|---|
   | `DATABASE_URL` | URL de Neon con prefijo `postgresql+psycopg://` y `sslmode=require` |
   | `SECRET_KEY` | Valor aleatorio de al menos 32 caracteres, generado fuera del repositorio |
   | `ENVIRONMENT` | `production` |
   | `AUTH_ENABLED` | `true` |
   | `CORS_ORIGINS` | Vacío para Android nativo; si hay cliente web, orígenes exactos separados por comas |
   | `AUTH_RATE_LIMIT_ATTEMPTS` | `10` |
   | `AUTH_RATE_LIMIT_WINDOW_SECONDS` | `60` |

   Si la URL de Neon empieza por `postgresql://`, cambia el prefijo a `postgresql+psycopg://` para SQLAlchemy. No guardes URLs ni secretos en Git.
5. Configura el health check como `/api/v1/health`. Render proporciona `PORT`; el contenedor ejecuta `alembic upgrade head` antes de iniciar Uvicorn.
6. Abre `https://<servicio>.onrender.com/api/v1/health` y confirma `{"status":"ok"}`. Swagger queda desactivado en producción.

El contenedor aplica migraciones al arrancar. Mantén la demo en una instancia; para varias, ejecuta migraciones una sola vez como tarea previa al despliegue.

## Backups

Neon Free no ofrece backups programados. En Windows, instala las herramientas cliente de PostgreSQL y ejecuta desde `Backend`:

```powershell
$env:DB_BACKUP_URL = "postgresql://<usuario>:<clave>@<host>/<base>?sslmode=require"
.\scripts\backup_database.ps1
```

El script guarda un dump privado en `$HOME\mimun-backups`, fuera del repositorio. Puedes programarlo con el Programador de tareas de Windows. `DB_BACKUP_URL` usa el formato nativo `postgresql://`, no el formato SQLAlchemy `postgresql+psycopg://`.

Restaura solo en una base vacía, usando `pg_restore` y una URL de destino separada:

```powershell
pg_restore --dbname $env:DB_RESTORE_URL --no-owner --no-acl "$HOME\mimun-backups\mimun-<fecha>.dump"
```