package com.snippetsearcher.permissions.permission;

import java.util.UUID;

// Quien pide no es el owner del snippet, y la operación es solo del owner.
public class NotOwnerException extends RuntimeException {

	public NotOwnerException(UUID snippetId) {
		super("Solo el owner puede hacer esto con el snippet " + snippetId);
	}
}
