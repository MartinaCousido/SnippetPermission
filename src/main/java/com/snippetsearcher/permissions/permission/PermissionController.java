package com.snippetsearcher.permissions.permission;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

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

	@ExceptionHandler(OwnerAlreadyRegisteredException.class)
	public ProblemDetail ownerAlreadyRegistered(OwnerAlreadyRegisteredException e) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
	}
}
