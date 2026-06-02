package com.weblog.apigateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
public class GatewayConfig {

  @Bean
  public CorsWebFilter corsFilter() {
	CorsConfiguration corsConfig = new CorsConfiguration();
	corsConfig.addAllowedOrigin("http://127.0.0.1:5000");
	corsConfig.setAllowCredentials(true);
	corsConfig.setMaxAge(3600L);
	corsConfig.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
	corsConfig.setAllowedHeaders(List.of("*"));

	UrlBasedCorsConfigurationSource source =
			new UrlBasedCorsConfigurationSource();
	source.registerCorsConfiguration("/**", corsConfig);

	return new CorsWebFilter(source);
  }
}
