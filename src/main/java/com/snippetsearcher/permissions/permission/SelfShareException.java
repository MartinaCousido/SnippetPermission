package com.snippetsearcher.permissions.permission;

import java.util.UUID;

// El owner quiso compartirse el snippet a sí mismo.
public class SelfShareException extends RuntimeException {

	public SelfShareException(UUID snippetId) {
		super("El owner no puede compartirse el snippet " + snippetId + " a sí mismo");
	}
}
