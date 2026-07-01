package com.weblog.authservice.controller;

import com.weblog.authservice.dto.request.LoginRequest;
import com.weblog.authservice.dto.request.RegisterRequest;
import com.weblog.authservice.dto.request.RoleUpdateRequest;
import com.weblog.authservice.dto.request.UpdateProfileRequest;
import com.weblog.authservice.dto.response.AuthResponse;
import com.weblog.authservice.dto.response.SessionResponse;
import com.weblog.authservice.dto.response.UserResponse;
import com.weblog.authservice.exception.InvalidUserIdException;
import com.weblog.authservice.service.session.SessionService;
import com.weblog.authservice.service.user.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

  // FIX 1: Drive secure flag from config, not hardcoded false.
  // Previously both login and logout had .secure(false) baked in, which
  // means refresh tokens travel over plain HTTP in any environment.
  // With this, application-prod.properties sets cookie.secure=true and
  // application-dev.properties sets it to false — no code change needed.
  @Value("${app.cookie.secure:true}")
  private boolean cookieSecure;

  // FIX 2: Centralize the cookie path as a constant.
  // It was written as a string literal in login, then written differently
  // ("/") in logout, so the browser couldn't find the cookie to delete it.
  // One constant guarantees they always match.
  private static final String REFRESH_COOKIE_PATH = "/";
  private static final String REFRESH_COOKIE_NAME = "refresh_token";

  private final UserService    userService;
  private final SessionService sessionService;

  @PostMapping("/register")
  public ResponseEntity <UserResponse> register(@Valid @RequestBody RegisterRequest request) {
	UserResponse response = userService.register(request);
	return ResponseEntity.ok(response);
  }

  @PostMapping("/login")
  public ResponseEntity <AuthResponse> login(@Valid @RequestBody LoginRequest request,
											 HttpServletRequest httpRequest,
											 HttpServletResponse httpResponse) {
	AuthResponse response = userService.login(request, httpRequest);

	// FIX 3: SameSite is now "Strict" consistently.
	// Logout previously used "Lax", login used "Strict" — pick one and
	// keep it everywhere. "Strict" is correct for an auth cookie.
	httpResponse.addHeader(
			HttpHeaders.SET_COOKIE,
			buildRefreshCookie(response.refreshToken(),
							   Duration.ofDays(7)).toString()
						  );

	return ResponseEntity.ok(new AuthResponse(
			response.user(),
			response.accessToken(),
			null // refresh token stays in the cookie, not the body
	));
  }

  @PutMapping(value = "/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity <UserResponse> updateProfile(
		  @RequestHeader("X-User-Id") String userId,
		  @RequestHeader("X-User-Role") String role,
		  @Valid @RequestPart("data") UpdateProfileRequest request,
		  @RequestPart(value = "avatar", required = false) MultipartFile avatar
													) {
	UUID id = convertUserId(userId);
	return ResponseEntity.ok(userService.updateProfile(id, request, avatar));
  }

  @PutMapping("/role")
  public ResponseEntity <Void> updateRole(
		  @RequestHeader("X-User-Id") String adminUserId,
		  @RequestHeader("X-User-Role") String adminRole,
		  @RequestParam("userId") String targetUserId,
		  @Valid @RequestBody RoleUpdateRequest request) {

	UUID adminId = UUID.fromString(adminUserId);
	UUID targetId = UUID.fromString(targetUserId);

	userService.updateRole(adminId, adminRole, targetId, request.role());
	return ResponseEntity.ok().build();
  }

  @GetMapping(value = "/count")
  public ResponseEntity <Long> countUsers(
		  @RequestHeader("X-User-Id") String userId,
		  @RequestHeader("X-User-Role") String role
										 ) {
	UUID id = convertUserId(userId);
	return ResponseEntity.ok(userService.countUsers(id, role));
  }

  // FIX 4: The refresh endpoint was completely missing.
  // UserServiceImpl.refresh() existed but nothing called it — dead code.
  // This reads the refresh token from the HttpOnly cookie (the only place
  // it lives after login), rotates the session, and issues a new cookie +
  // access token exactly like login does.
  @PostMapping("/refresh")
  public ResponseEntity <AuthResponse> refresh(
		  // FIX 5: required = false so a missing cookie returns 401 (our logic)
		  // rather than Spring's 400 Bad Request.
		  @CookieValue(value = REFRESH_COOKIE_NAME, required = false) String token,
		  HttpServletResponse httpResponse) {

	if(token == null) {
	  return ResponseEntity.status(401).build();
	}

	AuthResponse response = userService.refresh(token);

	httpResponse.addHeader(HttpHeaders.SET_COOKIE,
						   buildRefreshCookie(response.refreshToken(), Duration.ofDays(7)).toString());

	return ResponseEntity.ok(new AuthResponse(
			response.user(),
			response.accessToken(),
			null
	));
  }

  @PostMapping("/logout")
  public ResponseEntity <Void> logout(
		  @CookieValue(value = REFRESH_COOKIE_NAME, required = false) String token,
		  HttpServletResponse httpResponse) {

	if(token != null) {
	  userService.logout(token);
	}

	// FIX 2 (cont): expiry cookie uses the same path constant, so the
	// browser can actually locate and delete the cookie it set at login.
	httpResponse.addHeader(HttpHeaders.SET_COOKIE, buildExpiredCookie().toString());

	return ResponseEntity.ok().build();
  }

  @GetMapping("/users")
  public ResponseEntity <List <UserResponse>> getUsers(
		  @RequestHeader("X-User-Id") String userId,
		  @RequestHeader("X-User-Role") String role) {

	UUID id = convertUserId(userId);
	return ResponseEntity.ok(userService.listUsers(id, role));
  }


  @GetMapping("/mysessions")
  public ResponseEntity <List <SessionResponse>> getSessions(
		  @RequestHeader("X-User-Id") String userId,
		  @RequestHeader("X-User-Role") String role) {

	UUID id = convertUserId(userId);
	return ResponseEntity.ok(sessionService.getUserSessions(id, role));
  }

  @GetMapping("/sessions/{userId}")
  public ResponseEntity <List <SessionResponse>> getUserSessions(
		  @PathVariable String userId,
		  @RequestHeader("X-User-Role") String role) {

	UUID id = convertUserId(userId);
	return ResponseEntity.ok(sessionService.adminGetUserSessions(id, role));
  }

  // ── helpers ────────────────────────────────────────────────────────────────

  private ResponseCookie buildRefreshCookie(String value, Duration maxAge) {
	return ResponseCookie.from(REFRESH_COOKIE_NAME, value)
						 .httpOnly(true)
						 .secure(cookieSecure)
						 .path(REFRESH_COOKIE_PATH)
						 .maxAge(maxAge)
						 .sameSite("Lax")
						 .build();
  }

  private ResponseCookie buildExpiredCookie() {
	// Dedicated method so it's obvious this cookie is meant to evict,
	// not to set a new value. maxAge(0) tells the browser to delete it.
	return buildRefreshCookie("", Duration.ZERO);
  }

  private UUID convertUserId(String userId) {
	try {
	  return UUID.fromString(userId);
	} catch(IllegalArgumentException e) {
	  throw new InvalidUserIdException("Invalid user ID format: " + userId);
	}
  }
}
