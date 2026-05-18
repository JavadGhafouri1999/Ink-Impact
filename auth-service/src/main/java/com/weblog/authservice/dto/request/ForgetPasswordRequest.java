package com.weblog.authservice.dto.request;

import jakarta.validation.constraints.Email;

public record ForgetPasswordRequest(
		@Email(message = "A valid email is required")
		String email
) {
}
