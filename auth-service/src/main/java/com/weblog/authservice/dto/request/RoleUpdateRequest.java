package com.weblog.authservice.dto.request;

import com.weblog.authservice.model.User;
import jakarta.validation.constraints.NotNull;

public record RoleUpdateRequest(
		@NotNull
		String userId,
		@NotNull
		User.Role role
) { }
