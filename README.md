# SnippetPermission

Servicio de permisos de [Snippet Searcher](https://github.com/MartinaCousido/SnippetSearcher): registra al owner de cada snippet, gestiona con quién se comparte y responde quién puede leerlo o editarlo.

> La arquitectura, las user stories (US) y las decisiones técnicas del sistema completo están en el repo
> [SnippetSearcher](https://github.com/MartinaCousido/SnippetSearcher), que es la fuente de verdad.
> Este README cubre solo lo específico de este servicio.

## Responsabilidad

| Hace | No hace |
|---|---|
| Registrar al owner cuando se crea un snippet | Autenticar usuarios (lo hace **Auth0**) |
| Compartir un snippet con otro usuario (solo lectura) | Guardar el contenido ni los metadatos de los snippets (eso es de **Snippet Service**) |
| Responder si un usuario puede leer o escribir un snippet | Decidir permisos por rol (por ahora no hay roles en el sistema) |
| Listar los snippets a los que un usuario tiene acceso | |

## Reglas de permisos

- El **owner** es quien creó el snippet: puede leerlo, editarlo, borrarlo y compartirlo.
- Un usuario con quien se **compartió** el snippet solo puede leerlo, igual que sus tests (US-7).
- Solo el owner puede compartir.
- El `userId` siempre se obtiene del **JWT de Auth0** validado, nunca de un dato que manda el cliente.

## Relación con otros servicios

| Servicio | Relación |
|---|---|
| **UI** | Llama a este servicio para compartir snippets |
| **Snippet Service** | Registra al owner al crear un snippet y consulta los permisos antes de leer o modificar |
| **Auth0** | Emite los JWT que este servicio valida |

Este servicio no llama a ningún otro servicio del sistema. Su base de datos es propia y ningún otro servicio accede a ella.

## Modelo de datos (lógico)

| Dato | Descripción |
|---|---|
| `snippetId` | Id del snippet (lo genera Snippet Service) |
| `userId` | Id del usuario en Auth0 |
| `tipo` | `OWNER` o `LECTURA` |

## Operaciones

Contrato definido en [Decisiones técnicas](https://github.com/MartinaCousido/SnippetSearcher#decisiones-técnicas) de SnippetSearcher.

| Operación | Endpoint | Respuesta | Quién la usa | User story |
|---|---|---|---|---|
| Registrar owner | `POST /permissions/{snippetId}/owner` | 201. Si ya es el owner, no hace nada y responde 201 igual. **409** si el snippet ya tiene otro owner, o si el usuario ya tiene otro permiso sobre el snippet | Snippet Service | US-1, US-3 |
| Compartir snippet | `POST /permissions/{snippetId}/share` con `{ "userId": "auth0\|juan" }` | 201. 403 si quien comparte no es el owner | UI | US-7 |
| ¿Puede leer / escribir? | `GET /permissions/{snippetId}` | `{ "canRead": true, "canWrite": false }` | Snippet Service | US-2, US-4, US-6 |
| Snippets accesibles por un usuario | `GET /permissions/snippets?relation=owner\|shared\|all` | Lista de ids, por ejemplo `["42", "57"]` | Snippet Service | US-5 |

En todos, el `userId` de quien pide sale del JWT. Mientras no esté Auth0, es siempre el usuario fijo `dev-user` (`security/CurrentUser`), una decisión temporal registrada en [Decisiones técnicas](https://github.com/MartinaCousido/SnippetSearcher#decisiones-técnicas). Cuando el pedido viene de Snippet Service, este reenvía el token del usuario: nunca manda el `userId` en el cuerpo ni en un header.

## Stack

**Java 25 + Spring Boot 4.1.1 + Gradle 9.8**, con **PostgreSQL 17** propio. Las decisiones del sistema están en [Decisiones técnicas](https://github.com/MartinaCousido/SnippetSearcher#decisiones-técnicas) de SnippetSearcher.

## Imagen Docker

En cada push a `dev` o `main`, GitHub Actions (`.github/workflows/cd.yml`) arma la imagen con el `Dockerfile` y la publica en GHCR como `ghcr.io/martinacousido/permission-service`. Lleva dos tags: el del ambiente (`dev` o `prod`) y el del commit (`sha-…`). La imagen es pública: las VMs la descargan sin login. Ver [Tags de las imágenes](https://github.com/MartinaCousido/SnippetSearcher-infra#tags-de-las-imágenes) en el repo de infra.

Después de publicar la imagen, el mismo workflow entra por SSH a la VM del ambiente (`dev` → VM de dev, `main` → VM de prod) y corre `docker service update` con el tag `sha-…` del commit. Actualiza **solo** `permission-service`: los demás servicios del stack no se reinician. Usa los secrets `VM_HOST`, `VM_USER` y `VM_SSH_KEY` de los *Environments* `dev` y `prod` de este repo. La VM tiene que estar prendida.

## Cómo levantarlo

Requisitos: Docker Desktop abierto. No hace falta instalar Java ni Gradle: se compila dentro de Docker.

```bash
cp .env.example .env          # variables de la base (el .env no se commitea)
docker compose up --build     # Permission Service + su base
```

| Contenedor | URL / puerto en tu máquina |
|---|---|
| Permission Service | http://localhost:8081 |
| Postgres `permisos` | `localhost:5434` |

El puerto de Postgres solo se publica en `127.0.0.1`: la base no es accesible desde otras máquinas de la red.

Si se quiere cambiar la contraseña de la base, hay que hacerlo en el `.env` **antes del primer arranque**: Postgres la toma solo cuando crea la base por primera vez. Para cambiarla después hay que borrar los datos (`docker compose down -v`) y volver a levantar.

Para comprobar que está arriba y conectado a su base: http://localhost:8081/actuator/health tiene que responder `"status":"UP"`, con `db` también en `UP`.

Para levantar **el sistema completo** (todos los servicios), usar `docker-compose.sistema.yml` desde el repo [SnippetSearcher](https://github.com/MartinaCousido/SnippetSearcher#cómo-levantarlo), con este repo clonado al lado. Ese archivo incluye el `docker-compose.yml` de este repo y usa su `.env`. Todos comparten el proyecto de Docker `snippet-searcher`, así que los contenedores y los datos son los mismos se levante este servicio solo o el sistema completo.

### Correr los tests

El framework es **JUnit 5**, con los starters de test de Spring Boot (decisión registrada en SnippetSearcher). Los tests levantan el servicio completo, así que necesitan la base arriba y las variables de conexión. Con la base levantada (`docker compose up -d permisos-db`):

```bash
DB_URL=jdbc:postgresql://localhost:5434/permisos DB_USER=<usuario> DB_PASSWORD=<contraseña> ./gradlew test
```

Usuario y contraseña son los del `.env`. Sin la base, `./gradlew build` y `./gradlew test` fallan; la imagen de Docker se arma igual porque solo ejecuta `bootJar`.

En cada PR hacia `dev` o `main`, el CI (`.github/workflows/ci.yml`) corre `./gradlew build` con una base Postgres 17 descartable. Su check se llama `compilar-y-testear` y es obligatorio para mergear.

## Flujo de trabajo

Igual que en SnippetSearcher: ramas `feature/*` desde `dev`, PR hacia `dev`, y de `dev` a `main`. Las correcciones de los PRs las hace Martina Cousido.
