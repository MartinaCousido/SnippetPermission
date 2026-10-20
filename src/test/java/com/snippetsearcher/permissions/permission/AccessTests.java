package com.snippetsearcher.permissions.permission;

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

// GET /permissions/{snippetId} contra la base real. El usuario que pide es
// siempre dev-user.
@SpringBootTest
@AutoConfigureMockMvc
class AccessTests {

	private static final UUID OWN = UUID.fromString("11111111-1111-1111-1111-111111111111");
	private static final UUID SHARED = UUID.fromString("22222222-2222-2222-2222-222222222222");
	private static final UUID SOMEONE_ELSES = UUID.fromString("33333333-3333-3333-3333-333333333333");
	private static final UUID UNKNOWN = UUID.fromString("44444444-4444-4444-4444-444444444444");

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
	void theOwnerCanReadAndWrite() throws Exception {
		mvc.perform(get("/permissions/{id}", OWN))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.canRead").value(true))
				.andExpect(jsonPath("$.canWrite").value(true));
	}

	@Test
	void aUserItWasSharedWithCanOnlyRead() throws Exception {
		mvc.perform(get("/permissions/{id}", SHARED))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.canRead").value(true))
				.andExpect(jsonPath("$.canWrite").value(false));
	}

	@Test
	void anyOtherUserCanDoNothing() throws Exception {
		mvc.perform(get("/permissions/{id}", SOMEONE_ELSES))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.canRead").value(false))
				.andExpect(jsonPath("$.canWrite").value(false));
	}

	@Test
	void aSnippetWithoutPermissionsCannotBeSeen() throws Exception {
		mvc.perform(get("/permissions/{id}", UNKNOWN))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.canRead").value(false))
				.andExpect(jsonPath("$.canWrite").value(false));
	}

	@Test
	void anIdThatIsNotAUuidIsABadRequest() throws Exception {
		mvc.perform(get("/permissions/{id}", "42")).andExpect(status().isBadRequest());
	}
}
