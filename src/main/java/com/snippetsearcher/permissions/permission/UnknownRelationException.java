package com.snippetsearcher.permissions.permission;

public class UnknownRelationException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public UnknownRelationException(String relation) {
		super("Relación desconocida: " + relation + ". Puede ser owner, shared o all");
	}
}
