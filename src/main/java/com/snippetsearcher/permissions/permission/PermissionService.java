package com.snippetsearcher.permissions.permission;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PermissionService {

	private static final Logger log = LoggerFactory.getLogger(PermissionService.class);

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
				log.warn("el snippet {} ya tiene otro owner", snippetId);
				throw new OwnerAlreadyRegisteredException(snippetId);
			}
			return;
		}
		repository.save(new SnippetPermission(snippetId, userId, PermissionType.OWNER));
		log.info("owner registrado del snippet {}", snippetId);
	}

	// Comparte el snippet con otro usuario, que queda con permiso de solo
	// lectura (US-7). Solo el owner comparte. Compartir dos veces con el mismo
	// usuario no duplica el permiso.
	@Transactional
	public void share(UUID snippetId, String ownerId, String userId) {
		var owner = repository.findBySnippetIdAndType(snippetId, PermissionType.OWNER);
		if (owner.isEmpty() || !owner.get().userId().equals(ownerId)) {
			log.info("compartir rechazado: no es el owner del snippet {}", snippetId);
			throw new NotOwnerException(snippetId);
		}
		if (userId.equals(ownerId)) {
			throw new SelfShareException(snippetId);
		}
		if (repository.findBySnippetIdAndUserId(snippetId, userId).isEmpty()) {
			repository.save(new SnippetPermission(snippetId, userId, PermissionType.LECTURA));
			log.info("snippet {} compartido con {}", snippetId, userId);
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
			log.info("borrar permisos rechazado: no es el owner del snippet {}", snippetId);
			throw new NotOwnerException(snippetId);
		}
		repository.deleteBySnippetId(snippetId);
		log.info("permisos del snippet {} borrados", snippetId);
	}

	// Los snippets a los que el usuario tiene acceso, según la relación (US-5).
	@Transactional(readOnly = true)
	public List<UUID> accessibleSnippets(String userId, Relation relation) {
		var permissions = switch (relation) {
			case OWNER -> repository.findByUserIdAndType(userId, PermissionType.OWNER);
			case SHARED -> repository.findByUserIdAndType(userId, PermissionType.LECTURA);
			case ALL -> repository.findByUserId(userId);
		};
		log.info("snippets accesibles ({}): {}", relation, permissions.size());
		return permissions.stream().map(SnippetPermission::snippetId).toList();
	}
}
