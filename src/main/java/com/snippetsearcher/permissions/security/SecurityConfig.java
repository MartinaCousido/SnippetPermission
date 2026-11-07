package com.snippetsearcher.permissions.security;

import static org.springframework.security.config.Customizer.withDefaults;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

// Quién puede llamar a cada endpoint (ver Roles y permisos en Decisiones
// técnicas de SnippetSearcher). Cada pedido lleva el token de Auth0: el de la
// UI al compartir, o el que reenvía Snippet Service. Spring revisa la firma, el
// iss, el aud y que no esté vencido (application.properties). Sin token
// válido: 401. Sin el permiso que pide la operación: 403.
//
// Si el usuario es owner o tiene acceso compartido lo decide PermissionService.
//
// Spring convierte cada permiso del claim "scope" en una authority
// SCOPE_<permiso>.
@Configuration
@EnableWebSecurity
public class SecurityConfig {

	static final String READ = "SCOPE_read:snippets";
	static final String WRITE = "SCOPE_write:snippets";

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
				.authorizeHttpRequests(requests -> requests
						// El health lo usan los healthchecks de Docker, sin token.
						.requestMatchers(HttpMethod.GET, "/actuator/health", "/actuator/health/**").permitAll()
						// Leer: ¿puede leer o escribir? y los snippets accesibles.
						.requestMatchers(HttpMethod.GET, "/**").hasAuthority(READ)
						// Escribir: registrar owner, compartir y borrar permisos.
						.anyRequest().hasAuthority(WRITE))
				.oauth2ResourceServer(resourceServer -> resourceServer.jwt(withDefaults()))
				// Con tokens no hay sesión ni cookies, así que tampoco CSRF.
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.csrf(csrf -> csrf.disable())
				// Usa el CorsConfigurationSource de CorsConfig. Responde el
				// preflight (OPTIONS) antes de pedir el token.
				.cors(withDefaults());
		return http.build();
	}
}
