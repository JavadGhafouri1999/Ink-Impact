package com.weblog.authservice.dto.response;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(
		UUID id,
		String username,
		String email,
		String phone,
		String avatarUrl,
		String roles,
		
		Instant createdAt,
		Instant updatedAt
) {
}
