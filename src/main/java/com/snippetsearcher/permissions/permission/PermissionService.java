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

	// Comparte el snippet con otro usuario, que queda con permiso de solo
	// lectura (US-7). Solo el owner comparte. Compartir dos veces con el mismo
	// usuario no duplica el permiso.
	@Transactional
	public void share(UUID snippetId, String ownerId, String userId) {
		var owner = repository.findBySnippetIdAndType(snippetId, PermissionType.OWNER);
		if (owner.isEmpty() || !owner.get().userId().equals(ownerId)) {
			throw new NotOwnerException(snippetId);
		}
		if (userId.equals(ownerId)) {
			throw new SelfShareException(snippetId);
		}
		if (repository.findBySnippetIdAndUserId(snippetId, userId).isEmpty()) {
			repository.save(new SnippetPermission(snippetId, userId, PermissionType.LECTURA));
		}
	}

	// Si el usuario puede leer o escribir el snippet (US-2, US-4, US-6). Un
	// snippet sin permisos registrados no lo puede ver nadie.
	@Transactional(readOnly = true)
	public Access access(UUID snippetId, String userId) {
		return repository.findBySnippetIdAndUserId(snippetId, userId)
				.map(permission -> Access.of(permission.type()))
				.orElse(Access.NONE);
	}

	// Borra todos los permisos del snippet, cuando se borra el snippet. Solo el
	// owner. Si ya no tiene permisos no hace nada: así Snippet Service puede
	// reintentar.
	@Transactional
	public void deleteAll(UUID snippetId, String userId) {
		var owner = repository.findBySnippetIdAndType(snippetId, PermissionType.OWNER);
		if (owner.isPresent() && !owner.get().userId().equals(userId)) {
			throw new NotOwnerException(snippetId);
		}
		repository.deleteBySnippetId(snippetId);
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
