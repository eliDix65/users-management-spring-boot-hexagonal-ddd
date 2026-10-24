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

La capa de persistencia expone dos adaptadores equivalentes y selecciona uno según `db.vendor`:

| `DB_VENDOR` | Adaptador | Script de esquema |
| --- | --- | --- |
| `mysql` (por defecto) | `UserRepositoryMySQL` | `src/main/resources/schema.sql` |
| `postgresql` | `UserRepositoryPostgreSQL` | `src/main/resources/schema-postgresql.sql` |

Ambos adaptadores implementan los mismos puertos de salida, así que la capa de aplicación y el
dominio no cambian. Solo difieren en el SQL de las marcas de tiempo: MySQL admite `NOW()`, mientras
que PostgreSQL usa el estándar `CURRENT_TIMESTAMP` y actualiza `updated_at` explícitamente en la
sentencia `UPDATE` (PostgreSQL no tiene `ON UPDATE CURRENT_TIMESTAMP`).

### Desarrollo local con Docker

```bash
# MySQL (por defecto)
docker compose --profile mysql up --build

# PostgreSQL
DB_VENDOR=postgresql DB_HOST=postgres DB_PORT=5432 docker compose --profile postgres up --build
```

La API queda en `http://localhost:8080` y Swagger UI en `/swagger-ui.html`.

## Despliegue en Render

El archivo `render.yaml` define el servicio web, el build con Docker y el despliegue
automático de cada commit que llegue a la rama `main`.

1. Crear un Blueprint en Render y seleccionar este repositorio.
2. Completar en el panel los secretos marcados como requeridos: `DB_HOST`,
   `DB_USERNAME`, `DB_PASSWORD`, `SMTP_USERNAME`, `SMTP_PASSWORD` y
   `SMTP_FROM_ADDRESS`.
3. Ejecutar el script de esquema **correspondiente al motor configurado** una sola vez:
   `schema-postgresql.sql` para Supabase/PostgreSQL o `schema.sql` para MySQL.
   El script de PostgreSQL no crea la base: se provisiona desde el panel del proveedor
   y el script se ejecuta ya dentro de ella.
4. Desplegar el Blueprint. La API quedará disponible en el subdominio
   `onrender.com` asignado por Render y Swagger UI en `/swagger-ui.html`.

Las credenciales nunca deben guardarse en `application.properties` ni en
`render.yaml`. Para desarrollo local, deben proporcionarse como variables de entorno.
