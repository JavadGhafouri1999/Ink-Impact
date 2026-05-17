package com.weblog.apigateway.config;

import com.weblog.apigateway.kafka.UserActivityProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthFilter extends AbstractGatewayFilterFactory <Object> {
	
	private final JwtDecoder jwtDecoder;
	
	private final UserActivityProducer activityProducer;
	
	@Override
	public GatewayFilter apply(Object config) {
		return ((exchange, chain) -> {
			
			// 1) Read Authorization header
			String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
			
			// If no token → just forward (public endpoints can still work)
			if(authHeader == null || !authHeader.startsWith("Bearer ")) {
				log.debug("No Bearer token found, forwarding request without user context");
				return chain.filter(exchange);
			}
			
			// strip "Bearer "
			String token = authHeader.substring(7);
			
			return Mono.fromCallable(() -> jwtDecoder.decode(token))
			           .flatMap(jwt -> {
				           
				           // 3) Extract claims from Keycloak token
				           //    "user_id" and "roles" come from your Keycloak protocol mappers
				           String        userId = jwt.getClaimAsString("user_id");
				           List <String> roles  = jwt.getClaimAsStringList("roles");
				           
				           if(userId == null) {
					           log.warn("JWT does not contain 'user_id' claim, rejecting");
					           return Mono.error(
							           new IllegalStateException("Invalid token: missing user_id"));
				           }
				           
				           if(roles == null) { roles = List.of(); }
				           
				           // 4) Inject headers for downstream services (post-service, comment-service, etc.)
				           var mutatedRequest = exchange
						           .getRequest()
						           .mutate()
						           .header("X-User-Id", userId)
						           .header("X-User-Roles", String.join(",", roles))
						           .build();
				           
				           // 5) Emit Kafka activity event for session tracking / analytics
				           try {
					           String path = exchange.getRequest().getURI().getPath();
					           String method = exchange.getRequest().getMethod() != null
					                           ? exchange.getRequest().getMethod().name()
					                           : "UNKNOWN";
					           String ip = exchange.getRequest().getRemoteAddress() != null
					                       ? exchange.getRequest().getRemoteAddress().getAddress()
					                                 .getHostAddress()
					                       : "UNKNOWN";
					           String userAgent = exchange.getRequest().getHeaders().getFirst("User-Agent");
					           
					           activityProducer.sendActivityEvent(
							           userId,
							           roles,
							           path,
							           method,
							           ip,
							           userAgent);
				           } catch(Exception e) {
					           log.warn("Failed to send user activity event", e);
				           }
				           // 6) Continue filter chain with mutated request
				           return chain.filter(exchange.mutate().request(mutatedRequest).build());
			           });
		});
	}
}
