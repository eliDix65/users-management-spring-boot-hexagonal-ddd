# Users Management — Spring Boot, Arquitectura Hexagonal y DDD

Aplicación de gestión de usuarios construida con Java 17 y Spring Boot. La API REST es el punto de entrada activo. El código de la antigua CLI se conserva como adaptador inactivo y no posee un contenedor de dependencias independiente.

Spring es el único *composition root*: `Main` inicia el contexto y las dependencias se resuelven mediante configuración y component scanning de Spring.

## Verificación

```bash
./mvnw clean test
./mvnw clean package
```

En Windows se puede utilizar `mvnw.cmd`.

## Motores de base de datos soportados

La capa de persistencia expone dos adaptadores equivalentes y se selecciona uno segun `db.vendor`:

| `DB_VENDOR` | Adaptador | Script de esquema |
| --- | --- | --- |
| `mysql` (por defecto) | `UserRepositoryMySQL` | `src/main/resources/schema.sql` |
| `postgresql` | `UserRepositoryPostgreSQL` | `src/main/resources/schema-postgresql.sql` |

Ambos adaptadores implementan los mismos puertos de salida, asi que la capa de aplicacion y el
dominio no cambian.

### Desarrollo local con Docker

```bash
# MySQL (por defecto)
docker compose --profile mysql up --build

# PostgreSQL
DB_VENDOR=postgresql DB_HOST=postgres DB_PORT=5432 docker compose --profile postgres up --build
```

La API queda en `http://localhost:8080` y Swagger UI en `/swagger-ui.html`.

## Secretos para desarrollo local

Copiar `application-secrets.properties.example` como
`.environments/application-secrets.properties` y reemplazar los valores de ejemplo.
Este archivo contiene la conexion local a MySQL, la semilla JWT y las credenciales de
Brevo. La carpeta `.environments` esta excluida de Git y del contexto de Docker.

En produccion no se utiliza este archivo: los secretos deben configurarse mediante las
variables de entorno `DB_*`, `JWT_SECRET` y `BREVO_*` del proveedor de despliegue.

Copiar `compose.env.example` como `.env`, reemplazar todos los valores antes de ejecutar
`docker compose`: `DB_PASSWORD`, `MYSQL_ROOT_PASSWORD`, `JWT_SECRET`, `BREVO_API_KEY` y
`BREVO_FROM_ADDRESS` son obligatorios. Los puertos de MySQL y la API se publican solo en
`127.0.0.1`.

## Despliegue en Render

El archivo `render.yaml` define el servicio web, el build con Docker y el despliegue
automático de cada commit que llegue a la rama `main`.

1. Crear un Blueprint en Render y seleccionar este repositorio.
2. Completar en el panel los secretos marcados como requeridos: `DB_HOST`,
1. Crear un Blueprint en Render y seleccionar este repositorio.
2. Completar en el panel los secretos marcados como requeridos: `DB_HOST`,
   `DB_USERNAME`, `DB_PASSWORD`, `BREVO_API_KEY` y `BREVO_FROM_ADDRESS`.
3. Ejecutar el script de esquema **correspondiente al motor configurado** una sola vez:
   `schema.sql` para MySQL/TiDB o `schema-postgresql.sql` para Supabase/PostgreSQL.
   El script de PostgreSQL no crea la base: se provisiona desde el panel del proveedor
   y el script se ejecuta ya dentro de ella.
4. Desplegar el Blueprint. La API quedará disponible en el subdominio
   `onrender.com` asignado por Render y Swagger UI en `/swagger-ui.html`.

Las credenciales nunca deben guardarse en `application.properties` ni en
`render.yaml`. Para desarrollo local, deben proporcionarse como variables de entorno.

## Notificaciones por correo

Los correos transaccionales se envian con `POST /v3/smtp/email` de Brevo. La llamada
se ejecuta fuera del hilo HTTP en el executor `notificationExecutor` y dispone de
timeouts, reintentos para errores de red/408/429/5xx con backoff exponencial, circuit
breaker e idempotencia para evitar duplicados durante los reintentos.

Variables requeridas: `BREVO_API_KEY` y `BREVO_FROM_ADDRESS`. El remitente debe estar
verificado en Brevo. Los parametros de resiliencia `BREVO_CONNECT_TIMEOUT`,
`BREVO_READ_TIMEOUT`, `BREVO_RETRY_*` y `BREVO_CIRCUIT_*` son opcionales y tienen
valores predeterminados en `application.properties`.
