package com.weblog.authservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
		@Email(message = "A valid email is required")
		String email,
		
		@NotBlank(message = "Password can not be blank")
		String password
) {
}
