package com.snippetsearcher.permissions.permission;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PermissionService {

	private final PermissionRepository repository;

	public PermissionService(PermissionRepository repository) {
		this.repository = repository;
	}

	// Registra a userId como owner del snippet. Un snippet tiene un solo owner:
	// si ya es suyo no hace nada (así Snippet Service puede reintentar), y si es
	// de otro usuario falla.
	@Transactional
	public void registerOwner(UUID snippetId, String userId) {
		var owner = repository.findBySnippetIdAndType(snippetId, PermissionType.OWNER);
		if (owner.isPresent()) {
			if (!owner.get().userId().equals(userId)) {
				throw new OwnerAlreadyRegisteredException(snippetId);
			}
			return;
		}
		repository.save(new SnippetPermission(snippetId, userId, PermissionType.OWNER));
	}

	// Los snippets a los que el usuario tiene acceso, según la relación (US-5).
	@Transactional(readOnly = true)
	public List<UUID> accessibleSnippets(String userId, Relation relation) {
		var permissions = switch (relation) {
			case OWNER -> repository.findByUserIdAndType(userId, PermissionType.OWNER);
			case SHARED -> repository.findByUserIdAndType(userId, PermissionType.LECTURA);
			case ALL -> repository.findByUserId(userId);
		};
		return permissions.stream().map(SnippetPermission::snippetId).toList();
	}
}
