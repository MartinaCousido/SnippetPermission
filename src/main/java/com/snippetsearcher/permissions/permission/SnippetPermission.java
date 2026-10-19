package com.snippetsearcher.permissions.permission;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// Un permiso de un usuario sobre un snippet. Es una fila de la tabla permissions.
@Entity
@Table(name = "permissions")
public class SnippetPermission {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "snippet_id", nullable = false)
	private UUID snippetId;

	@Column(name = "user_id", nullable = false)
	private String userId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private PermissionType type;

	protected SnippetPermission() {
		// Lo pide JPA.
	}

	public SnippetPermission(UUID snippetId, String userId, PermissionType type) {
		this.snippetId = snippetId;
		this.userId = userId;
		this.type = type;
	}

	public UUID snippetId() {
		return snippetId;
	}

	public String userId() {
		return userId;
	}

	public PermissionType type() {
		return type;
	}
}
