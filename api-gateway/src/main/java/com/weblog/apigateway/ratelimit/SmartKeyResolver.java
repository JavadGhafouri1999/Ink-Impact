package com.weblog.apigateway.ratelimit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;

@Slf4j
@Component("smartKeyResolver")
public class SmartKeyResolver implements KeyResolver {

@Override
public Mono <String> resolve(ServerWebExchange exchange) {
  log.info("Rate limiter key resolver called for path: {}", exchange.getRequest().getURI().getPath());
  // 1. If authenticated, use user ID (best and safest)
  String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");
  if(userId != null && !userId.isBlank()) {
	log.info("Using user ID key: user:{}", userId);
	return Mono.just("user:" + userId);
  }
  // 2. If behind proxy/load balancer, use X-Forwarded-For
  String forwardedFor = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
  if(forwardedFor != null && !forwardedFor.isBlank()) {
	String clientIp = forwardedFor.split(",")[0].trim();
	log.info("Using X-Forwarded-For IP key: ip:{}", clientIp);
	return Mono.just("ip:" + clientIp);
  }
  // 3. Try direct remote address (might be null in Docker)
  InetSocketAddress remote = exchange.getRequest().getRemoteAddress();
  if(remote != null && remote.getAddress() != null) {
	log.info("Using remote address key: ip:{}", remote.getAddress().getHostAddress());
	return Mono.just("ip:" + remote.getAddress().getHostAddress());
  }

  // 4. Fallback (never return null)
  log.warn("Using fallback anonymous key");
  return Mono.just("anonymous");
}
}
