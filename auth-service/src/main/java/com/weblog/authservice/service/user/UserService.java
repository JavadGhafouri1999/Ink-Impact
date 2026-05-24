package com.weblog.authservice.service.user;

import com.weblog.authservice.dto.request.LoginRequest;
import com.weblog.authservice.dto.request.RegisterRequest;
import com.weblog.authservice.dto.response.AuthResponse;
import com.weblog.authservice.dto.response.UserResponse;
import jakarta.servlet.http.HttpServletRequest;

public interface UserService {

UserResponse register(RegisterRequest request);

AuthResponse login(LoginRequest request, HttpServletRequest httpRequest);

AuthResponse refresh(String refreshToken);

void logout(String refreshToken);
}
