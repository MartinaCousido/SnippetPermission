package com.snippetsearcher.permissions.permission;

import java.util.UUID;

// El snippet ya tiene otro owner.
public class OwnerAlreadyRegisteredException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public OwnerAlreadyRegisteredException(UUID snippetId) {
		super("El snippet " + snippetId + " ya tiene owner");
	}
}
