# SnippetPermission — reglas para Claude

Este servicio es parte de Snippet Searcher. Las reglas, la arquitectura y las decisiones técnicas viven en el repo **SnippetSearcher**, que es la fuente de verdad. Estas reglas aplican a toda tarea en este repo.

## Antes de actuar

- Leer este `README.md` y, del repo SnippetSearcher: `CLAUDE.md`, `README.md` (en especial **Decisiones técnicas**, **Arquitectura** y **Flujo de trabajo**), `docs/` y `docs/diagramas/`.
  - Si SnippetSearcher está clonado al lado (`../SnippetSearcher`), leerlo desde ahí.
  - Si no, leerlo desde GitHub: `gh repo view MartinaCousido/SnippetSearcher` y `gh api repos/MartinaCousido/SnippetSearcher/contents/<ruta>`.
- Para escribir código, seguir el proceso de la skill `desarrollar-snippetsearcher` de SnippetSearcher (`.claude/skills/desarrollar-snippetsearcher/SKILL.md`).
- Seguir lo definido en esos archivos. Si algo que la tarea necesita no está definido, o figura como **Pendiente** o **A confirmar**, preguntar a Martina antes de decidir. No inventar stack, versiones, endpoints, contratos entre servicios ni reglas de negocio.
- Un cambio de contrato de este servicio afecta a Snippet Service y a la UI: se acuerda y se registra en SnippetSearcher antes de implementarlo.

## Git

- Nunca commitear directo a `main` ni a `dev`. Cada tarea va en `feature/us-<n>-<descripcion>` (o `docs/`, `fix/`…) creada desde `dev`.
- Commits en español: `tipo(scope): resumen` en imperativo, máximo 72 caracteres. Tipos: `feat`, `fix`, `docs`, `refactor`, `test`, `chore`, `ci`. Scope = `permisos` o el área tocada (`docs`, `infra`). El cuerpo explica el porqué; el pie referencia la user story (`Refs: US-7`). Un cambio lógico por commit.
- Pushear y abrir PRs solo cuando Martina lo pida o lo confirme.

## PRs

- Los PRs van hacia `dev`. Lo aprobado en `dev` pasa a `main` con un PR `dev` → `main`.
- **Las correcciones de los PRs las hace Martina Cousido.** Claude no aprueba ni mergea PRs.
- Antes de proponer un PR, Claude hace una autocorrección con la skill `corregir-prs` de SnippetSearcher y arregla lo que encuentre.
