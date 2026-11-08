package com.snippetsearcher.permissions.security;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

// Dice quién es el usuario que hace el pedido. Es el único lugar del servicio
// que lo sabe: el userId nunca se toma de un dato que manda el cliente.
// Es el "sub" del token de Auth0, por ejemplo auth0|abc123, que Spring ya
// validó (SecurityConfig). Cuando pide Snippet Service, es el token del
// usuario que este reenvía.
@Component
public class CurrentUser {

	public String id() {
		var authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication instanceof JwtAuthenticationToken jwt) {
			return jwt.getName();
		}
		throw new IllegalStateException("El pedido no tiene un usuario autenticado");
	}
}
