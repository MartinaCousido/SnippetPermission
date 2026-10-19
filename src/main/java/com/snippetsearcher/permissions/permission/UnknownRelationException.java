package com.snippetsearcher.permissions.permission;

public class UnknownRelationException extends RuntimeException {

	public UnknownRelationException(String relation) {
		super("Relación desconocida: " + relation + ". Puede ser owner, shared o all");
	}
}
