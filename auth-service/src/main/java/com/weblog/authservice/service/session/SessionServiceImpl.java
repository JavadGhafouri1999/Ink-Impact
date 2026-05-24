package com.weblog.authservice.service.session;

import com.weblog.authservice.dto.response.SessionResponse;
import com.weblog.authservice.exception.UnauthorizedException;
import com.weblog.authservice.model.Session;
import com.weblog.authservice.model.User;
import com.weblog.authservice.repository.SessionRepository;
import com.weblog.authservice.security.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SessionServiceImpl implements SessionService {

private final SessionRepository sessionRepo;
private final JwtService        jwtService;

@Override
@Transactional
public Session createSession(User user, HttpServletRequest request) {
  String ip = getClientIp(request);
  String userAgent = request.getHeader("User-Agent");

  // Deactivate all other sessions
  sessionRepo.deactivateAllUserSessions(user.getId());

  // create new session
  Session session = new Session(true,
								Instant.now().plusSeconds(604800),
								ip,
								user,
								userAgent);

  return sessionRepo.save(session);
}

@Override
@Transactional
public boolean validateSession(String refreshToken) {

  // First validate the token signature and expiration
  if(!jwtService.validateToken(refreshToken)) {
	return false;
  }

  UUID sessionId = jwtService.extractSessionId(refreshToken);

  return sessionRepo.findByIdAndActive(sessionId, true).map(session -> {
	if(session.getExpiresAt().isBefore(Instant.now())) {
	  session.setActive(false);
	  sessionRepo.save(session);
	  return false;
	}
	if(session.getExpiresAt().isBefore(Instant.now().plusSeconds(86400))) {
	  session.setExpiresAt(Instant.now().plusSeconds(604800)); // Extend by 7 days
	  sessionRepo.save(session);
	}
	return true;
  }).orElse(false);
}

@Override
@Transactional
public Session refreshSession(String oldRefreshToken) {

  // First validate the token signature and expiration
  if(!jwtService.validateToken(oldRefreshToken)) {
	throw new UnauthorizedException("Invalid refresh token");
  }

  UUID oldSessionId = jwtService.extractSessionId(oldRefreshToken);

  return sessionRepo.findById(oldSessionId).map(oldSession -> {
	// Invalidate old session
	oldSession.setActive(false);
	oldSession.setRevokedAt(Instant.now());
	sessionRepo.save(oldSession);

	// Create new session
	Session newSession = new Session(true, Instant.now()
												  .plusSeconds(604800), oldSession.getIpAddress(),
									 oldSession.getUser(),
									 oldSession.getUserAgent());
	return sessionRepo.save(newSession);
  }).orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));
}

@Override
@Transactional
public void logout(String token) {

  if(!jwtService.validateToken(token)) {
	throw new UnauthorizedException("Invalid refresh token");
  }

  UUID sessionId = jwtService.extractSessionId(token);

  sessionRepo.findById(sessionId).ifPresent(session -> {
	session.setActive(false);
	session.setRevokedAt(Instant.now());
	sessionRepo.save(session);
  });
}

@Override
public List <SessionResponse> getUserSessions(UUID userId) {
  return sessionRepo.findByUserIdAndActive(userId, true).stream().map(SessionResponse::toDto).toList();
}

private String getClientIp(HttpServletRequest request) {
  String xForwardedFor = request.getHeader("X-Forwarded-For");
  if(xForwardedFor != null && !xForwardedFor.isEmpty()) {
	return xForwardedFor.split(",")[0].trim();
  }
  return request.getRemoteAddr();
}
}
