package com.weblog.authservice.dto.mapper;

import com.weblog.authservice.dto.request.RegisterRequest;
import com.weblog.authservice.dto.response.UserResponse;
import com.weblog.authservice.model.User;

public final class UserMapper {
  
  private UserMapper() { }
  
  public static User toModel(RegisterRequest request) {
	User user = new User();
	user.setEmail(request.email());
	user.setUsername(request.username());
	user.setPassword(request.password());
	
	return user;
  }
  
  public static UserResponse toDto(User user) {
	return new UserResponse(
			user.getId(),
			user.getUsername(),
			user.getEmail(),
			user.getPhone(),
			user.getAvatarUrl(),
			String.valueOf(user.getRole()),
			user.getCreatedAt(),
			user.getUpdatedAt()
	);
  }
}
