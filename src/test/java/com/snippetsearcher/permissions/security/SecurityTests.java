package com.snippetsearcher.permissions.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import com.snippetsearcher.permissions.permission.PermissionRepository;

// Quién puede llamar a cada endpoint (SecurityConfig): sin token, 401; sin el
// permiso que pide la operación, 403. Si es owner o tiene acceso compartido se
// prueba en los tests de cada operación.
@SpringBootTest(properties = "cors.allowed-origins=https://snippetsearcher-dev.duckdns.org")
@AutoConfigureMockMvc
class SecurityTests {

	private static final UUID SNIPPET = UUID.fromString("33333333-3333-3333-3333-333333333333");

	@Autowired
	private MockMvc mvc;

	@Autowired
	private PermissionRepository repository;

	@BeforeEach
	void setUp() {
		repository.deleteAll();
	}

	@Test
	void withoutATokenItIsUnauthorized() throws Exception {
		mvc.perform(get("/permissions/snippets")).andExpect(status().isUnauthorized());
		mvc.perform(post("/permissions/{id}/owner", SNIPPET)).andExpect(status().isUnauthorized());
		assertThat(repository.count()).isZero();
	}

	@Test
	void theHealthDoesNotNeedAToken() throws Exception {
		mvc.perform(get("/actuator/health")).andExpect(status().isOk());
	}

	// El usuario sin rol: tiene sesión, pero su token no trae permisos.
	@Test
	void withoutPermissionsItIsForbidden() throws Exception {
		var sinRol = TestUsers.user("auth0|sin-rol", "");

		mvc.perform(get("/permissions/snippets").with(sinRol)).andExpect(status().isForbidden());
		mvc.perform(post("/permissions/{id}/owner", SNIPPET).with(sinRol)).andExpect(status().isForbidden());
		assertThat(repository.count()).isZero();
	}

	@Test
	void readingOnlyNeedsReadSnippets() throws Exception {
		var lector = TestUsers.user("auth0|lector", "read:snippets");

		mvc.perform(get("/permissions/snippets").with(lector)).andExpect(status().isOk());
		mvc.perform(get("/permissions/{id}", SNIPPET).with(lector)).andExpect(status().isOk());
	}

	@Test
	void writingNeedsWriteSnippets() throws Exception {
		var lector = TestUsers.user("auth0|lector", "read:snippets");

		mvc.perform(post("/permissions/{id}/owner", SNIPPET).with(lector)).andExpect(status().isForbidden());
		assertThat(repository.count()).isZero();
	}

	// El owner queda registrado con el "sub" del token.
	@Test
	void theOwnerIsTheUserOfTheToken() throws Exception {
		mvc.perform(post("/permissions/{id}/owner", SNIPPET).with(TestUsers.user("auth0|leo", "write:snippets")))
				.andExpect(status().isCreated());

		assertThat(repository.findAll()).singleElement()
				.satisfies(permission -> assertThat(permission.userId()).isEqualTo("auth0|leo"));
	}

	// El navegador manda el preflight sin token: tiene que responderse igual.
	@Test
	void thePreflightDoesNotNeedAToken() throws Exception {
		mvc.perform(options("/permissions/{id}/share", SNIPPET)
						.header("Origin", "https://snippetsearcher-dev.duckdns.org")
						.header("Access-Control-Request-Method", "POST")
						.header("Access-Control-Request-Headers", "Authorization, Content-Type"))
				.andExpect(status().isOk())
				.andExpect(header().string("Access-Control-Allow-Origin", "https://snippetsearcher-dev.duckdns.org"));
	}
}
