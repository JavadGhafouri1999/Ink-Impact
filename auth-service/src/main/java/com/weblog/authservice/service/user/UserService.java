package com.weblog.authservice.service.user;

import com.weblog.authservice.dto.request.LoginRequest;
import com.weblog.authservice.dto.request.RegisterRequest;
import com.weblog.authservice.dto.request.UpdateProfileRequest;
import com.weblog.authservice.dto.response.AuthResponse;
import com.weblog.authservice.dto.response.UserResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface UserService {

  UserResponse register(RegisterRequest request);

  AuthResponse login(LoginRequest request, HttpServletRequest httpRequest);

  AuthResponse refresh(String refreshToken);

  UserResponse updateProfile(UUID userId, UpdateProfileRequest request, MultipartFile avatar);

  void logout(String refreshToken);

  long countUsers(UUID userId, String role);

  List <UserResponse> listUsers(UUID userId, String role);
}
