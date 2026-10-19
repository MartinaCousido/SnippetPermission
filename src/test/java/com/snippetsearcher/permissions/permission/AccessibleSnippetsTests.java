package com.snippetsearcher.permissions.permission;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import com.snippetsearcher.permissions.security.CurrentUser;

// GET /permissions/snippets?relation= contra la base real.
@SpringBootTest
@AutoConfigureMockMvc
class AccessibleSnippetsTests {

	private static final UUID OWN = UUID.fromString("11111111-1111-1111-1111-111111111111");
	private static final UUID SHARED = UUID.fromString("22222222-2222-2222-2222-222222222222");
	private static final UUID SOMEONE_ELSES = UUID.fromString("33333333-3333-3333-3333-333333333333");

	@Autowired
	private MockMvc mvc;

	@Autowired
	private PermissionRepository repository;

	@BeforeEach
	void setUp() {
		repository.deleteAll();
		repository.save(new SnippetPermission(OWN, CurrentUser.DEV_USER, PermissionType.OWNER));
		repository.save(new SnippetPermission(SHARED, "auth0|otro", PermissionType.OWNER));
		repository.save(new SnippetPermission(SHARED, CurrentUser.DEV_USER, PermissionType.LECTURA));
		repository.save(new SnippetPermission(SOMEONE_ELSES, "auth0|otro", PermissionType.OWNER));
	}

	@Test
	void ownerReturnsOnlyTheUsersOwnSnippets() throws Exception {
		mvc.perform(get("/permissions/snippets").param("relation", "owner"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", containsInAnyOrder(OWN.toString())));
	}

	@Test
	void sharedReturnsOnlyTheSnippetsSharedWithTheUser() throws Exception {
		mvc.perform(get("/permissions/snippets").param("relation", "shared"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", containsInAnyOrder(SHARED.toString())));
	}

	@Test
	void allReturnsBoth() throws Exception {
		mvc.perform(get("/permissions/snippets").param("relation", "all"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", containsInAnyOrder(OWN.toString(), SHARED.toString())));
	}

	@Test
	void withoutARelationItReturnsAll() throws Exception {
		mvc.perform(get("/permissions/snippets"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", containsInAnyOrder(OWN.toString(), SHARED.toString())));
	}

	@Test
	void aUserWithoutSnippetsGetsAnEmptyList() throws Exception {
		repository.deleteAll();

		mvc.perform(get("/permissions/snippets"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", empty()));
	}

	@Test
	void anUnknownRelationIsABadRequest() throws Exception {
		mvc.perform(get("/permissions/snippets").param("relation", "amigos"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.detail").value("Relación desconocida: amigos. Puede ser owner, shared o all"));
	}
}
