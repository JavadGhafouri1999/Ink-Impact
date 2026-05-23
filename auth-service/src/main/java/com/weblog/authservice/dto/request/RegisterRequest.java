package com.weblog.authservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record RegisterRequest(
		@NotBlank(message = "Username is required")
		@Pattern(
				regexp = "^[a-zA-Z0-9_]{3,20}$",
				message =
						"Username must be "
						+ "3-20 characters "
						+ "long and can only"
						+ " "
						+ "contain letters, "
						+ "numbers, "
						+ "underscores")
		String username,
		@Email(message = "A valid email is required") String email,
		/**
		 * Accepts Iranian numbers Examples it accepts:
		 *
		 * <p>09121234567
		 *
		 * <p>9121234567
		 *
		 * <p>+989121234567
		 *
		 * <p>+98 912 123 4567
		 *
		 * <p>Accepts European numbers Examples it accepts:
		 *
		 * <p>+4915123456789
		 */
		@Pattern(
				regexp = "^(\\+?[1-9]\\d{0,3})?[-.\\s]?\\d{6,14}$",
				message = "Invalid phone number format")
		String phone,

		// Strong password: at least 8 chars, 1 uppercase, 1 lowercase, 1 number, 1 special char
		@NotBlank(message = "Password is required")
		@Pattern(
				regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$",
				message =
						"Password must contain at least 8 characters, one digit, one lowercase, one "
						+ "uppercase, and one special character")
		String password) { }
