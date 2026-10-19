package com.snippetsearcher.permissions.permission;

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
}
