package com.snippetsearcher.permissions.permission;

// Con quién se comparte el snippet: el userId de Auth0 del invitado.
public record ShareRequest(String userId) {
}
