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

A nivel lógico. Los endpoints y formatos concretos están pendientes (ver [Decisiones técnicas](https://github.com/MartinaCousido/SnippetSearcher#decisiones-técnicas)).

| Operación | Quién la usa | User story |
|---|---|---|
| Registrar owner | Snippet Service | US-1, US-3 |
| Compartir snippet | UI | US-7 |
| ¿Puede leer / escribir? | Snippet Service | US-2, US-4, US-6 |
| Snippets accesibles por un usuario | Snippet Service | US-5 |

## Stack y cómo correrlo

⏳ **Pendiente.** Se define junto con el resto del sistema en el repo SnippetSearcher.

## Flujo de trabajo

Igual que en SnippetSearcher: ramas `feature/*` desde `dev`, PR hacia `dev`, y de `dev` a `main`. Las correcciones de los PRs las hace Martina Cousido.
