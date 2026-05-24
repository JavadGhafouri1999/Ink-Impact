package com.weblog.authservice.controller;

import com.weblog.authservice.dto.request.LoginRequest;
import com.weblog.authservice.dto.request.RegisterRequest;
import com.weblog.authservice.dto.response.AuthResponse;
import com.weblog.authservice.dto.response.UserResponse;
import com.weblog.authservice.service.user.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping("/api/auth/")
@RequiredArgsConstructor
public class AuthController {

private final UserService userService;

@PostMapping("register")
public ResponseEntity <UserResponse> register(@Valid @RequestBody RegisterRequest request) {
  UserResponse response = userService.register(request);
  return ResponseEntity.ok(response);
}

@PostMapping("login")
public ResponseEntity <AuthResponse> login(@Valid @RequestBody LoginRequest request,
										   HttpServletRequest httpRequest,
										   HttpServletResponse httpResponse) {
  AuthResponse response = userService.login(request, httpRequest);

  ResponseCookie refreshCookie = ResponseCookie
		  .from("refresh_token", response.refreshToken())
		  .httpOnly(true)
		  .secure(false)
		  .path("/api/auth/refresh")
		  .maxAge(Duration.ofDays(7))
		  .sameSite("Strict")
		  .build();

  httpResponse.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
  // Return ONLY access token + user
  return ResponseEntity.ok(
		  new AuthResponse(
				  response.user(),
				  response.accessToken(),
				  null // hide refresh token
		  )
						  );
}


@PostMapping("/logout")
public ResponseEntity <Void> logout(@CookieValue("refresh_token") String token) {
  userService.logout(token);

  // Create an expired cookie to force the browser to delete it
  ResponseCookie cleanCookie = ResponseCookie.from("refresh_token", "")
											 .path("/api/auth/refresh")
											 .maxAge(0) // Expire immediately
											 .httpOnly(true)
											 .build();

  return ResponseEntity.ok()
					   .header(HttpHeaders.SET_COOKIE, cleanCookie.toString())
					   .build();
}
}
