package com.weblog.authservice.security;

import com.weblog.authservice.model.Session;
import com.weblog.authservice.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;
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

  // FIX 1: Cache the signing key instead of re-deriving it on every call.
  // Previously getKey() was called inside every method, which re-ran
  // Keys.hmacShaKeyFor(secret.getBytes(...)) on each token operation.
  // @PostConstruct runs once after Spring injects @Value fields, so the
  // key is built exactly once for the lifetime of the bean.
  private SecretKey signingKey;

  @PostConstruct
  private void init() {
	byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);

	// FIX 2: Fail fast on weak secrets.
	// HMAC-SHA256 needs at least 256 bits (32 bytes) to be considered
	// secure. A short or low-entropy string from config (e.g. "secret123")
	// would silently produce a weak key before. Now we throw at startup
	// instead of discovering it in prod.
	if (keyBytes.length < 32) {
	  throw new IllegalStateException(
			  "jwt.secret must be at least 32 bytes — current length: " + keyBytes.length);
	}

	this.signingKey = Keys.hmacShaKeyFor(keyBytes);
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
	                         .signWith(signingKey) // FIX 1 (cont): use cached key
	                         .compact();

	log.info("Generated access token for user-ID: {}", user.getId());
	return accessToken;
  }

  public String generateRefreshToken(User user, Session session) {
	long now = System.currentTimeMillis();

	// FIX 3: Strip refresh token down to the minimum claims.
	// Previously it carried username, role, and sessionId. The only thing
	// a refresh token needs to do is identify the session — everything else
	// (user identity, role) is fetched fresh from the DB during the refresh
	// flow anyway. Putting role in a 7-day token means a role change isn't
	// reflected until it expires. sessionId + type is sufficient.
	String refreshToken = Jwts.builder()
	                          .subject(user.getId().toString())
	                          .claim("sessionId", session.getId().toString())
	                          .claim("type", "refresh")
	                          .issuedAt(new Date(now))
	                          .expiration(new Date(now + refreshTokenExpMs))
	                          .signWith(signingKey) // FIX 1 (cont): use cached key
	                          .compact();

	log.info("Generated refresh token for user-ID: {}", user.getId());
	return refreshToken;
  }

  // FIX 4: Replace the separate validateToken + extractClaims pattern with a
  // single Optional-returning method. The old design had two problems:
  //
  //   a) Double-parse: callers called validateToken (which parsed the token),
  //      then called extractClaims / extractSessionId (which parsed it again).
  //
  //   b) Silent throw: extractClaims had no try/catch, so any caller that
  //      skipped validateToken — or raced between the two calls — would get
  //      an uncaught JwtException bubbling up as a 500.
  //
  // Now callers get back Optional.empty() on any failure and parse exactly once.
  public Optional<Claims> tryExtractClaims(String token) {
	try {
	  Claims claims = Jwts.parser()
	                      .verifyWith(signingKey)
	                      .build()
	                      .parseSignedClaims(token)
	                      .getPayload();
	  return Optional.of(claims);
	} catch (Exception e) {
	  // FIX 5: Downgrade from log.error to log.warn.
	  // An expired or malformed token is an expected condition (not a server
	  // error), so ERROR was flooding logs with noise. WARN is appropriate
	  // for a security-relevant rejection; use DEBUG if it gets too chatty.
	  log.warn("Token parse failed: {}", e.getMessage());
	  return Optional.empty();
	}
  }

  // Kept as a convenience for code that only needs a boolean — but now
  // delegates to tryExtractClaims so there is no duplicated parse logic.
  public boolean validateToken(String token) {
	return tryExtractClaims(token).isPresent();
  }

  public boolean isRefreshToken(String token) {
	return tryExtractClaims(token)
			.map(claims -> "refresh".equals(claims.get("type")))
			.orElse(false);
  }

  // NOTE: kept for cases where the caller has already validated (e.g. filter
  // chains that call validateToken first). Throws intentionally here because
  // calling extractClaims on an unvalidated token is a programmer error, not
  // a user error — a runtime exception is the correct signal.
  public Claims extractClaims(String token) {
	return tryExtractClaims(token)
			.orElseThrow(() -> new IllegalArgumentException(
					"Cannot extract claims from an invalid or expired token"));
  }

  public UUID extractUserId(String token) {
	return UUID.fromString(extractClaims(token).getSubject());
  }

  public UUID extractSessionId(String token) {
	return UUID.fromString(extractClaims(token).get("sessionId", String.class));
  }

  public Date extractExpiration(String token) {
	return extractClaims(token).getExpiration();
  }
}
