package com.weblog.apigateway.ratelimit;

import java.net.InetSocketAddress;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component("smartKeyResolver")
public class SmartKeyResolver implements KeyResolver {

  @Override
  public Mono<String> resolve(ServerWebExchange exchange) {
    // 1. If authenticated, use user ID (best and safest)
    String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");
    if (userId != null && !userId.isBlank()) {
      return Mono.just("user:" + userId);
    }
    // 2. If behind proxy/load balancer, use X-Forwarded-For
    String forwardedFor = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
    if (forwardedFor != null && !forwardedFor.isBlank()) {
      String clientIp = forwardedFor.split(",")[0].trim();
      return Mono.just("ip:" + clientIp);
    }
    // 3. Try direct remote address (might be null in Docker)
    InetSocketAddress remote = exchange.getRequest().getRemoteAddress();
    if (remote != null && remote.getAddress() != null) {
      return Mono.just("ip:" + remote.getAddress().getHostAddress());
    }

    // 4. Fallback (never return null)
    return Mono.just("anonymous");
  }
}
