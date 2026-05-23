package com.weblog.authservice.dto.response;

public record AuthResponse(
		UserResponse use,
		String refreshToken,
		String accessToken) { }
