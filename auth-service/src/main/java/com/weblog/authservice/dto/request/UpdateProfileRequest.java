package com.weblog.authservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UpdateProfileRequest(

		@NotBlank(message = "Username is required")
		@Pattern(regexp = "^[a-zA-Z0-9_]{3,20}$",
				 message = "Username must be 3-20 characters long and can only contain letters, numbers, " +
						   "underscores")
		String username,

		@NotBlank(message = "Password is required")
		@Pattern(regexp =
						 "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$",
				 message =
						 "Password must contain at least 8 characters, one digit, one lowercase, one " +
						 "uppercase, and one special character")
		String password,

		@Pattern(regexp = "^(\\+\\d{1,3}[-.]?)?\\d{10,14}$",
				 message = "Phone number must be 10-14 digits, can start with + and country code  e.g : " +
						   "+12345678901")
		String phoneNumber
) {
}
