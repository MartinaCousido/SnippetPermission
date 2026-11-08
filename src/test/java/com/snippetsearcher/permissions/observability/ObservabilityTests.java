package com.snippetsearcher.permissions.observability;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import com.snippetsearcher.permissions.permission.PermissionRepository;
import com.snippetsearcher.permissions.security.TestUsers;

// El requestId de cada pedido (el que manda Snippet Service, o uno nuevo) y
// los logs de los cambios de permisos.
@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(OutputCaptureExtension.class)
class ObservabilityTests {

	@Autowired
	private MockMvc mvc;

	@Autowired
	private PermissionRepository repository;

	@BeforeEach
	void setUp() {
		repository.deleteAll();
	}

	@Test
	void keepsTheRequestIdOfSnippetServiceOrCreatesOne() throws Exception {
		mvc.perform(get("/permissions/snippets").header("X-Request-Id", "del-snippet-service").with(TestUsers.ana()))
				.andExpect(header().string("X-Request-Id", "del-snippet-service"));
		var created = mvc.perform(get("/permissions/snippets").with(TestUsers.ana()))
				.andReturn().getResponse().getHeader("X-Request-Id");
		assertThat(created).hasSize(36);
	}

	@Test
	void registeringAnOwnerIsLogged(CapturedOutput output) throws Exception {
		var snippet = UUID.randomUUID();

		mvc.perform(post("/permissions/{id}/owner", snippet).with(TestUsers.ana())).andExpect(status().isCreated());

		assertThat(output).contains("owner registrado del snippet " + snippet);
	}
}
