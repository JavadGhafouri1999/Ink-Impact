package com.weblog.authservice.security;

import com.weblog.authservice.model.Session;
import com.weblog.authservice.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JwtService {

private static final Logger log = LoggerFactory.getLogger(JwtService.class);

@Value("${jwt.secret}")
private String secret;

@Value("${jwt.access-token-expiration}")
private long accessTokenExpMs;

@Value("${jwt.refresh-token-expiration}")
private long refreshTokenExpMs;

private SecretKey getKey() {
  return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
}

public String generateAccessToken(User user, Session session) {
  long now = System.currentTimeMillis();

  String accessToken = Jwts.builder()
						   .subject(user.getId().toString())
						   .claim("username", user.getUsername())
						   .claim("sessionId", session.getId().toString())
						   .claim("role", user.getRole().name())
						   .claim("type", "access")
						   .issuedAt(new Date(now))
						   .expiration(new Date(now + accessTokenExpMs))
						   .signWith(getKey())
						   .compact();
  log.info("Generated Access-Token for user-ID: {}", user.getId());
  return accessToken;
}

public String generateRefreshToken(User user, Session session) {
  long now = System.currentTimeMillis();

  String refreshToken = Jwts.builder()
							.subject(user.getId().toString())
							.claim("username", user.getUsername())
							.claim("sessionId", session.getId().toString())
							.claim("role", user.getRole().name())
							.claim("type", "refresh")
							.issuedAt(new Date(now))
							.expiration(new Date(now + refreshTokenExpMs))
							.signWith(getKey())
							.compact();
  log.info("Generated Refresh-Token for user-ID: {}", user.getId());
  return refreshToken;
}

public boolean validateToken(String token) {
  try {
	Jwts.parser().verifyWith(getKey()).build().parseSignedClaims(token);
	return true;
  } catch(Exception e) {
	log.error("Token validation failed: {}", e.getMessage());
	return false;
  }
}

public boolean isRefreshToken(String token) {
  try {
	Claims claims = Jwts.parser().verifyWith(getKey()).build().parseSignedClaims(token).getPayload();
	return "refresh".equals(claims.get("type"));
  } catch(Exception e) {
	return false;
  }
}

public Claims extractClaims(String token) {
  return Jwts.parser().verifyWith(getKey()).build().parseSignedClaims(token).getPayload();
}

public UUID extractUserId(String token) {
  return UUID.fromString(extractClaims(token).getSubject());
}

public UUID extractSessionId(String token) {
  Claims claims = extractClaims(token);
  String sessionId = claims.get("sessionId", String.class);
  return UUID.fromString(sessionId);
}

public Date extractExpiration(String token) {
  return extractClaims(token).getExpiration();
}
}
