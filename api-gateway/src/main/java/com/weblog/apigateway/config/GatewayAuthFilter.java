package com.weblog.apigateway.config;

import com.weblog.apigateway.kafka.UserActivityProducer;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class GatewayAuthFilter implements GlobalFilter, Ordered {

  private static final Logger        log              =
		  LoggerFactory.getLogger(GatewayAuthFilter.class);
  // ── Paths ──────────────────────────────────────────────
  private static final List <String> PUBLIC_GET_PATHS =
		  List.of("/api/posts/**", "/api/comments/**");

  private static final List <String> PROTECTED_GET_OVERRIDES =
		  List.of("/api/posts/admin");

  private static final List <String>              PUBLIC_POST_PATHS     =
		  List.of("/api/auth/login",
				  "/api/auth/register",
				  "/api/auth/refresh");
  private static final Map <String, Set <String>> ROLE_RESTRICTED_PATHS =
		  Map.of(
				  "/api/posts/**", Set.of("AUTHOR", "ADMIN", "OWNER"),
				  "/api/comments/**", Set.of("USER", "AUTHOR", "ADMIN", "OWNER"),
				  "/api/admin/**", Set.of("ADMIN", "OWNER"),
				  "/api/analytics/**", Set.of("ADMIN", "OWNER"));
  private final        SecretKey                  secretKey;
  private final        AntPathMatcher             matcher               = new AntPathMatcher();
  private final        UserActivityProducer       activityProducer;

  // ── Helper-classes ─────────────────────────────────────
  public GatewayAuthFilter(@Value("${jwt.secret}") String secret, UserActivityProducer activityProducer) {
	this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

	this.activityProducer = activityProducer;
  }

  private boolean matches(String path, List <String> patterns) {
	return patterns.stream().anyMatch(p -> matcher.match(p, path));
  }

  private boolean matches(String path, String pattern) {
	return matcher.match(pattern, path);
  }

  private String extractToken(ServerWebExchange exchange) {
	String header = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
	if(header != null && header.startsWith("Bearer ")) {
	  return header.substring(7);
	}
	return null;
  }

  private String findMatchingRestrictedPath(String path) {
	return ROLE_RESTRICTED_PATHS.keySet()
								.stream()
								.filter(pattern -> matches(path, pattern))
								.findFirst()
								.orElse(null);
  }

  // ── Main-functionality ─────────────────────────────────
  @Override
  public Mono <Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

	String path = exchange.getRequest().getURI().getPath();

	String method = exchange.getRequest().getMethod().name();

	// ── Publics ─────────────────────────────────────────────
	if(method.equals("GET") && matches(path, PUBLIC_GET_PATHS) && !matches(path, PROTECTED_GET_OVERRIDES)) {
	  return chain.filter(exchange);
	}
	if(method.equals("POST") && matches(path, PUBLIC_POST_PATHS)) {
	  return chain.filter(exchange);
	}

	// ── Jwt-required ────────────────────────────────────────
	// * check if token is real, not expired, not tampered with, invalid → 401.

	String token = extractToken(exchange);

	// ! If you don’t have a token → get out.
	if(token == null) {
	  exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
	  return exchange.getResponse().setComplete();
	}
	Claims claims;
	try {
	  claims = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
	} catch(Exception e) {
	  log.error("Something went wrong validating token: {}", e.getMessage());
	  exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
	  return exchange.getResponse().setComplete();
	}
	// * What is your role? USER-AUTHOR-ADMIN-OWNER
	String role = claims.get("role", String.class);
	String userId = claims.getSubject();
	String username = claims.get("username", String.class);
	String ip = exchange.getRequest().getRemoteAddress() != null ? exchange.getRequest()
																		   .getRemoteAddress()
																		   .getAddress()
																		   .getHostAddress() : "unknown";

	String userAgent = exchange.getRequest().getHeaders().getFirst("User-Agent");

	// ── Role-based-paths ────────────────────────────────────
	// * checks whether the user’s role is allowed to access the path they are hitting

	String restrictedPattern = findMatchingRestrictedPath(path);

	if(restrictedPattern != null) {
	  Set <String> allowedRoles = ROLE_RESTRICTED_PATHS.get(restrictedPattern);

	  if(!allowedRoles.contains(role)) {
		exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
		return exchange.getResponse().setComplete();
	  }
	}

	// ── Send User Activity Event ────────────────────────────
	activityProducer.sendActivityEvent(userId, List.of(role), path, method, ip, userAgent);

	// ── Forward-user-info ───────────────────────────────────
	ServerWebExchange mutated = exchange.mutate().request(r -> r.headers(h -> {
	  h.add("X-User-Id", claims.getSubject());
	  h.add("X-User-Username", claims.get("username", String.class));
	  h.add("X-User-Role", role);
	})).build();

	return chain.filter(mutated);
  }

  /**
   * Controls when this filter runs compared to other filters. Spring Cloud Gateway has a chain of
   * filters. Lower number = higher priority.
   */
  @Override
  public int getOrder() {
	return -1;
  }
}
