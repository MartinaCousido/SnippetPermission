package com.snippetsearcher.permissions.observability;

import java.io.IOException;
import java.util.UUID;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import com.snippetsearcher.permissions.security.CurrentUser;

// Carga el contexto de cada pedido en los logs (MDC): requestId, usuario y
// servicio. El requestId llega de Snippet Service (X-Request-Id): así las
// líneas de un mismo pedido se ven juntas en los dos servicios. El agente de
// New Relic lo manda como atributos.
public class RequestContextFilter extends OncePerRequestFilter {

	static final String REQUEST_ID_HEADER = "X-Request-Id";
	static final String SERVICE_NAME = "permission-service";

	private final CurrentUser currentUser;

	public RequestContextFilter(CurrentUser currentUser) {
		this.currentUser = currentUser;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		var requestId = request.getHeader(REQUEST_ID_HEADER);
		if (requestId == null || requestId.isBlank() || requestId.length() > 100) {
			requestId = UUID.randomUUID().toString();
		}
		MDC.put("requestId", requestId);
		MDC.put("service", SERVICE_NAME);
		currentUser.idIfAuthenticated().ifPresent(user -> MDC.put("user", user));
		response.setHeader(REQUEST_ID_HEADER, requestId);
		try {
			chain.doFilter(request, response);
		} finally {
			MDC.remove("requestId");
			MDC.remove("service");
			MDC.remove("user");
		}
	}
}
