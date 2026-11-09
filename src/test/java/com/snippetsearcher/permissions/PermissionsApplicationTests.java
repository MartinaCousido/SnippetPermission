package com.snippetsearcher.permissions;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import com.snippetsearcher.permissions.permission.PermissionService;

@SpringBootTest
class PermissionsApplicationTests {

	@Autowired
	private ApplicationContext context;

	// El servicio arranca con su configuración y el servicio de permisos queda listo.
	@Test
	void contextLoads() {
		assertNotNull(context.getBean(PermissionService.class));
	}

}
