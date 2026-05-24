package com.weblog.authservice.dto.response;

public record AuthResponse(
		UserResponse user,
		String accessToken,
		String refreshToken
) { }
