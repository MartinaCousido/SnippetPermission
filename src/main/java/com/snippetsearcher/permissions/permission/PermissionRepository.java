package com.snippetsearcher.permissions.permission;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionRepository extends JpaRepository<SnippetPermission, Long> {

	Optional<SnippetPermission> findBySnippetIdAndType(UUID snippetId, PermissionType type);

	List<SnippetPermission> findByUserId(String userId);

	List<SnippetPermission> findByUserIdAndType(String userId, PermissionType type);
}
