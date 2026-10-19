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

// POST /permissions/{snippetId}/owner contra la base real.
@SpringBootTest
@AutoConfigureMockMvc
class RegisterOwnerTests {

	@Autowired
	private MockMvc mvc;

	@Autowired
	private PermissionRepository repository;

	@BeforeEach
	void cleanDatabase() {
		repository.deleteAll();
	}

	private ResultActions registerOwner(Object snippetId) throws Exception {
		return mvc.perform(post("/permissions/{snippetId}/owner", snippetId));
	}

	@Test
	void theCurrentUserBecomesTheOwner() throws Exception {
		var snippetId = UUID.randomUUID();

		registerOwner(snippetId).andExpect(status().isCreated());

		var owner = repository.findBySnippetIdAndType(snippetId, PermissionType.OWNER);
		assertThat(owner).get().extracting(SnippetPermission::userId).isEqualTo(CurrentUser.DEV_USER);
	}

	@Test
	void registeringTheSameOwnerTwiceKeepsASingleOwner() throws Exception {
		var snippetId = UUID.randomUUID();

		registerOwner(snippetId).andExpect(status().isCreated());
		registerOwner(snippetId).andExpect(status().isCreated());

		assertThat(repository.count()).isEqualTo(1);
	}

	@Test
	void aSnippetOwnedBySomeoneElseIsRejected() throws Exception {
		var snippetId = UUID.randomUUID();
		repository.save(new SnippetPermission(snippetId, "auth0|otro", PermissionType.OWNER));

		registerOwner(snippetId).andExpect(status().isConflict());

		var owner = repository.findBySnippetIdAndType(snippetId, PermissionType.OWNER);
		assertThat(owner).get().extracting(SnippetPermission::userId).isEqualTo("auth0|otro");
	}

	@Test
	void theOwnerIsNeverTakenFromTheBody() throws Exception {
		var snippetId = UUID.randomUUID();

		mvc.perform(post("/permissions/{snippetId}/owner", snippetId)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"userId\": \"auth0|intruso\"}"))
				.andExpect(status().isCreated());

		var owner = repository.findBySnippetIdAndType(snippetId, PermissionType.OWNER);
		assertThat(owner).get().extracting(SnippetPermission::userId).isEqualTo(CurrentUser.DEV_USER);
	}

	@Test
	void anIdThatIsNotAUuidIsABadRequest() throws Exception {
		registerOwner("no-es-un-uuid").andExpect(status().isBadRequest());
		assertThat(repository.count()).isZero();
	}
}
