package com.weblog.apigateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Slf4j
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {
	/**
	 This config makes the gateway behave as a pure JWT resource server.
	 
	 - No login page
	 
	 - No session management
	 
	 - No redirects
	 
	 - Only validates JWTs using the issuer/jwk-set-uri from application.yml
	 
	 JwtAuthFilter (your custom filter) runs AFTER this validation.
	 */
	@Bean
	public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
		return http
				.csrf(ServerHttpSecurity.CsrfSpec::disable)
				.formLogin(ServerHttpSecurity.FormLoginSpec::disable)
				.httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
				.oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
				.authorizeExchange(
						exchange -> exchange
								.anyExchange().permitAll())
				.build();
	}
}
