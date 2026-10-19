package com.snippetsearcher.permissions.permission;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionRepository extends JpaRepository<SnippetPermission, Long> {

	Optional<SnippetPermission> findBySnippetIdAndType(UUID snippetId, PermissionType type);
}
