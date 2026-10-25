package com.snippetsearcher.permissions.permission;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import com.snippetsearcher.permissions.security.CurrentUser;

// DELETE /permissions/{snippetId} contra la base real.
@SpringBootTest
@AutoConfigureMockMvc
class DeletePermissionsTests {

	private static final UUID SNIPPET = UUID.fromString("11111111-1111-1111-1111-111111111111");
	private static final UUID OTRO_SNIPPET = UUID.fromString("22222222-2222-2222-2222-222222222222");

	@Autowired
	private MockMvc mvc;

	@Autowired
	private PermissionRepository repository;

	@BeforeEach
	void cleanDatabase() {
		repository.deleteAll();
	}

	@Test
	void theOwnerDeletesEveryPermissionOfTheSnippet() throws Exception {
		repository.save(new SnippetPermission(SNIPPET, CurrentUser.DEV_USER, PermissionType.OWNER));
		repository.save(new SnippetPermission(SNIPPET, "auth0|juan", PermissionType.LECTURA));
		repository.save(new SnippetPermission(OTRO_SNIPPET, CurrentUser.DEV_USER, PermissionType.OWNER));

		mvc.perform(delete("/permissions/{snippetId}", SNIPPET)).andExpect(status().isNoContent());

		assertThat(repository.findAll()).extracting(SnippetPermission::snippetId).containsExactly(OTRO_SNIPPET);
	}

	@Test
	void someoneWhoIsNotTheOwnerCannotDeleteThem() throws Exception {
		repository.save(new SnippetPermission(SNIPPET, "auth0|otro", PermissionType.OWNER));
		repository.save(new SnippetPermission(SNIPPET, CurrentUser.DEV_USER, PermissionType.LECTURA));

		mvc.perform(delete("/permissions/{snippetId}", SNIPPET)).andExpect(status().isForbidden());

		assertThat(repository.count()).isEqualTo(2);
	}

	// Así Snippet Service puede reintentar si no recibió la respuesta.
	@Test
	void aSnippetWithoutPermissionsIsNotAnError() throws Exception {
		mvc.perform(delete("/permissions/{snippetId}", SNIPPET)).andExpect(status().isNoContent());
	}
}
