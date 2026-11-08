package com.snippetsearcher.permissions.observability;

import org.springframework.boot.security.autoconfigure.web.servlet.SecurityFilterProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.snippetsearcher.permissions.security.CurrentUser;

@Configuration
public class ObservabilityConfig {

	// Corre después de Spring Security: así ya se sabe quién es el usuario.
	@Bean
	public FilterRegistrationBean<RequestContextFilter> logContextFilter(CurrentUser currentUser) {
		var registration = new FilterRegistrationBean<>(new RequestContextFilter(currentUser));
		registration.setOrder(SecurityFilterProperties.DEFAULT_FILTER_ORDER + 1);
		return registration;
	}
}
