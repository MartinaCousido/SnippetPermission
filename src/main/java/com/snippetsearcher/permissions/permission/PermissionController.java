package com.snippetsearcher.permissions.permission;

import java.util.List;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.snippetsearcher.permissions.security.CurrentUser;

@RestController
@RequestMapping("/permissions")
public class PermissionController {

	private final PermissionService service;
	private final CurrentUser currentUser;

	public PermissionController(PermissionService service, CurrentUser currentUser) {
		this.service = service;
		this.currentUser = currentUser;
	}

	// Lo llama Snippet Service al crear un snippet (US-1, US-3). El owner es
	// quien hace el pedido: no viene en el cuerpo.
	@PostMapping("/{snippetId}/owner")
	@ResponseStatus(HttpStatus.CREATED)
	public void registerOwner(@PathVariable UUID snippetId) {
		service.registerOwner(snippetId, currentUser.id());
	}

	// Lo llama la UI para compartir un snippet (US-7). Quien comparte es quien
	// hace el pedido; el invitado viene en el cuerpo.
	@PostMapping("/{snippetId}/share")
	@ResponseStatus(HttpStatus.CREATED)
	public void share(@PathVariable UUID snippetId, @RequestBody ShareRequest request) {
		if (request.userId() == null || request.userId().isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Falta el userId del invitado");
		}
		service.share(snippetId, currentUser.id(), request.userId());
	}

	// Lo llama Snippet Service para armar el listado (US-5). Devuelve los ids de
	// los snippets del usuario que pide: propios (owner), compartidos (shared) o
	// todos (all, si no se indica).
	@GetMapping("/snippets")
	public List<UUID> accessibleSnippets(@RequestParam(defaultValue = "all") String relation) {
		return service.accessibleSnippets(currentUser.id(), Relation.fromValue(relation));
	}

	// Lo llama Snippet Service antes de mostrar o modificar un snippet.
	@GetMapping("/{snippetId}")
	public Access access(@PathVariable UUID snippetId) {
		return service.access(snippetId, currentUser.id());
	}

	// Lo llama Snippet Service al borrar un snippet: borra todos sus permisos,
	// para no dejar permisos de un snippet que ya no existe.
	@DeleteMapping("/{snippetId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteAll(@PathVariable UUID snippetId) {
		service.deleteAll(snippetId, currentUser.id());
	}

	@ExceptionHandler(UnknownRelationException.class)
	public ProblemDetail unknownRelation(UnknownRelationException e) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
	}

	@ExceptionHandler(SelfShareException.class)
	public ProblemDetail selfShare(SelfShareException e) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
	}

	@ExceptionHandler(NotOwnerException.class)
	public ProblemDetail notOwner(NotOwnerException e) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, e.getMessage());
	}

	@ExceptionHandler(OwnerAlreadyRegisteredException.class)
	public ProblemDetail ownerAlreadyRegistered(OwnerAlreadyRegisteredException e) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
	}

	// La base rechazó el permiso: el usuario ya tiene otro permiso sobre el
	// snippet, o dos pedidos quisieron registrar al owner a la vez.
	@ExceptionHandler(DataIntegrityViolationException.class)
	public ProblemDetail conflictingPermission() {
		return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, "El permiso choca con uno que ya existe");
	}
}
