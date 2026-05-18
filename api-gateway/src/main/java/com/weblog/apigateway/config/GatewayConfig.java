package com.weblog.apigateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
public class GatewayConfig {
  
  @Bean
  public CorsWebFilter corsFilter() {
	CorsConfiguration corsConfig = new CorsConfiguration();
	corsConfig.addAllowedOrigin("*");
	corsConfig.setMaxAge(3600L);
	corsConfig.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH"));
	corsConfig.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
	
	UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
	source.registerCorsConfiguration("/**", corsConfig);
	
	return new CorsWebFilter(source);
  }
}
