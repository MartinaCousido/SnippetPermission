package com.snippetsearcher.permissions.permission;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.snippetsearcher.permissions.security.CurrentUser;

// POST /permissions/{snippetId}/share contra la base real (US-7).
@SpringBootTest
@AutoConfigureMockMvc
class ShareSnippetTests {

	private static final UUID SNIPPET = UUID.fromString("11111111-1111-1111-1111-111111111111");
	private static final String JUAN = "auth0|juan";

	@Autowired
	private MockMvc mvc;

	@Autowired
	private PermissionRepository repository;

	@Autowired
	private PermissionService service;

	@BeforeEach
	void cleanDatabase() {
		repository.deleteAll();
	}

	private ResultActions share(String body) throws Exception {
		return mvc.perform(post("/permissions/{snippetId}/share", SNIPPET)
				.contentType(MediaType.APPLICATION_JSON)
				.content(body));
	}

	private void devUserOwnsTheSnippet() {
		repository.save(new SnippetPermission(SNIPPET, CurrentUser.DEV_USER, PermissionType.OWNER));
	}

	@Test
	void theOwnerSharesTheSnippetAsReadOnly() throws Exception {
		devUserOwnsTheSnippet();

		share("{\"userId\": \"auth0|juan\"}").andExpect(status().isCreated());

		assertThat(repository.findBySnippetIdAndUserId(SNIPPET, JUAN))
				.get().extracting(SnippetPermission::type).isEqualTo(PermissionType.LECTURA);
	}

	// Lo que pide el "Listo cuando": el invitado lee, no escribe, y lo ve
	// entre sus compartidos.
	@Test
	void theGuestCanReadItAndSeesItAmongTheShared() throws Exception {
		devUserOwnsTheSnippet();

		share("{\"userId\": \"auth0|juan\"}").andExpect(status().isCreated());

		assertThat(service.access(SNIPPET, JUAN)).isEqualTo(new Access(true, false));
		assertThat(service.accessibleSnippets(JUAN, Relation.SHARED)).containsExactly(SNIPPET);
		assertThat(service.accessibleSnippets(JUAN, Relation.OWNER)).isEmpty();
	}

	@Test
	void sharingTwiceWithTheSameUserKeepsASinglePermission() throws Exception {
		devUserOwnsTheSnippet();

		share("{\"userId\": \"auth0|juan\"}").andExpect(status().isCreated());
		share("{\"userId\": \"auth0|juan\"}").andExpect(status().isCreated());

		assertThat(repository.count()).isEqualTo(2);
	}

	@Test
	void someoneWhoIsNotTheOwnerCannotShareIt() throws Exception {
		repository.save(new SnippetPermission(SNIPPET, "auth0|otro", PermissionType.OWNER));
		repository.save(new SnippetPermission(SNIPPET, CurrentUser.DEV_USER, PermissionType.LECTURA));

		share("{\"userId\": \"auth0|juan\"}").andExpect(status().isForbidden());

		assertThat(repository.findBySnippetIdAndUserId(SNIPPET, JUAN)).isEmpty();
	}

	// Un snippet sin owner registrado no es de quien pide.
	@Test
	void aSnippetWithoutPermissionsCannotBeShared() throws Exception {
		share("{\"userId\": \"auth0|juan\"}").andExpect(status().isForbidden());

		assertThat(repository.count()).isZero();
	}

	@Test
	void theOwnerCannotShareItWithThemself() throws Exception {
		devUserOwnsTheSnippet();

		share("{\"userId\": \"dev-user\"}").andExpect(status().isBadRequest());

		assertThat(repository.count()).isEqualTo(1);
	}

	@Test
	void theGuestIsRequired() throws Exception {
		devUserOwnsTheSnippet();

		share("{}").andExpect(status().isBadRequest());
		share("{\"userId\": \"  \"}").andExpect(status().isBadRequest());

		assertThat(repository.count()).isEqualTo(1);
	}
}
