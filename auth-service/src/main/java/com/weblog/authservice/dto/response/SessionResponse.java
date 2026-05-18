package com.weblog.authservice.dto.response;

import com.weblog.authservice.model.Session;

import java.time.Instant;
import java.util.UUID;


public record SessionResponse(
		UUID id,
		String ipAddress,
		String userAgent,
		boolean active,
		Instant createdAt,
		Instant expiresAt
) {
  public static SessionResponse toDto(Session s) {
	return new SessionResponse(
			s.getId(),
			s.getIpAddress(),
			s.getUserAgent(),
			s.isActive(),
			s.getCreatedAt(),
			s.getExpiresAt()
	);
  }
}
