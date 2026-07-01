package com.weblog.authservice.service.user;

import com.weblog.authservice.dto.mapper.UserMapper;
import com.weblog.authservice.dto.request.LoginRequest;
import com.weblog.authservice.dto.request.RegisterRequest;
import com.weblog.authservice.dto.request.UpdateProfileRequest;
import com.weblog.authservice.dto.response.AuthResponse;
import com.weblog.authservice.dto.response.UserResponse;
import com.weblog.authservice.exception.ConflictException;
import com.weblog.authservice.exception.UnauthorizedException;
import com.weblog.authservice.exception.UserNotFoundException;
import com.weblog.authservice.model.Session;
import com.weblog.authservice.model.User;
import com.weblog.authservice.repository.UserRepository;
import com.weblog.authservice.security.JwtService;
import com.weblog.authservice.service.session.SessionService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private final UserRepository     userRepo;
  private final PasswordEncoder    passwordEncoder;
  private final JwtService         jwtService;
  private final SessionService     sessionService;
  private final FileStorageService fileStorageService;

  @Override
  @Transactional
  public UserResponse register(RegisterRequest request) {

	String email = request.email().toLowerCase().trim();
	String phone = request.phone().trim();
	String username = request.username().trim();

	// Single DB hit for all unique fields
	List <User> conflicts = userRepo.findConflicts(email, username, phone);

	for(User u : conflicts) {
	  if(u.getEmail().equals(email)) {
		throw new ConflictException("Email already taken");
	  }
	  if(u.getUsername().equals(username)) {
		throw new ConflictException("Username already taken");
	  }
	  if(u.getPhone().equals(phone)) {
		throw new ConflictException("Phone already taken");
	  }
	}

	String encodedPassword = passwordEncoder.encode(request.password());

	User user = UserMapper.toModel(request);
	user.setEmail(email);
	user.setUsername(username);
	user.setPhone(phone);
	user.setPassword(encodedPassword);
	user.setRole(User.Role.USER);

	User saved = userRepo.save(user);

	return UserMapper.toDto(saved);
  }

  @Override
  @Transactional
  public AuthResponse login(LoginRequest request, HttpServletRequest httpRequest) {

	String email = request.email().toLowerCase().trim();

	User user = userRepo.findByEmail(email)
						.orElseThrow(() -> new UnauthorizedException("Invalid  Credentials!"));

	if(!passwordEncoder.matches(request.password(), user.getPassword())) {
	  throw new UnauthorizedException("Invalid Credentials!");
	}

	Session session = sessionService.createSession(user, httpRequest);

	String accessToken = jwtService.generateAccessToken(user, session);
	String refreshToken = jwtService.generateRefreshToken(user, session);

	return new AuthResponse(
			UserMapper.toDto(user),
			accessToken,
			refreshToken);
  }

  @Override
  @Transactional
  public AuthResponse refresh(String oldRefreshToken) {

	if(!jwtService.validateToken(oldRefreshToken)) {
	  throw new UnauthorizedException("Invalid refresh token");
	}

	// 2. Delegate session rotation to SessionService
	Session newSession = sessionService.refreshSession(oldRefreshToken);

	User user = newSession.getUser();
	String newAccessToken = jwtService.generateAccessToken(user, newSession);
	String newRefreshToken = jwtService.generateRefreshToken(user, newSession);

	return new AuthResponse(
			UserMapper.toDto(user),
			newAccessToken,
			newRefreshToken
	);
  }

  @Override
  @Transactional
  public UserResponse updateProfile(UUID userId, UpdateProfileRequest request, MultipartFile avatar) {

	User user = userRepo.findById(userId)
						.orElseThrow(() -> new UserNotFoundException("User was not found"));

	if(avatar != null && !avatar.isEmpty()) {

	  String oldAvatar = user.getAvatarUrl();

	  String newAvatar = fileStorageService.saveAvatar(avatar);

	  user.setAvatarUrl(newAvatar);

	  if(oldAvatar != null && !oldAvatar.isBlank()) {
		fileStorageService.deleteAvatar(oldAvatar);
	  }
	}

	user.setUsername(request.username());
	user.setPhone(request.phoneNumber());

	if(request.password() != null
	   && !request.password().isBlank()) {

	  user.setPassword(
			  passwordEncoder.encode(request.password())
					  );
	}

	User saved = userRepo.save(user);

	return UserMapper.toDto(saved);
  }

  @Override
  @Transactional
  public void updateRole(UUID adminId, String adminRole, UUID targetUserId, User.Role newRole) {
	// Only OWNER can update roles
	if(!"OWNER".equals(adminRole)) {
	  throw new UnauthorizedException("Only OWNER can update user roles");
	}

	// Prevent owner from demoting themselves
	if(adminId.equals(targetUserId) && !newRole.equals(User.Role.OWNER)) {
	  throw new IllegalArgumentException("Cannot change your own role");
	}

	User targetUser = userRepo.findById(targetUserId)
							  .orElseThrow(() -> new UserNotFoundException(
									  "User not found with id: " + targetUserId));

	// Prevent setting OWNER role if not already owner (optional security)
	if(newRole.equals(User.Role.OWNER) && !targetUser.getRole().equals(User.Role.OWNER)) {
	  throw new UnauthorizedException("Only existing OWNER can assign OWNER role");
	}

	targetUser.setRole(newRole);
	userRepo.save(targetUser);
  }

  @Override
  @Transactional
  public void logout(String refreshToken) {
	sessionService.logout(refreshToken);
  }

  @Override
  @Transactional(readOnly = true)
  public long countUsers(UUID userId, String role) {
	if(!role.equals("ADMIN") && !role.equals("OWNER")) {
	  throw new UnauthorizedException("Unauthorized Access");
	}

	// Use count query instead of loading all users
	return userRepo.count();
  }

  @Override
  @Transactional(readOnly = true)
  public List <UserResponse> listUsers(UUID userId, String role) {
	if(!role.equals("ADMIN") && !role.equals("OWNER")) {
	  throw new UnauthorizedException("Unauthorized Access");
	}

	return userRepo.findAll().stream().map(UserMapper::toDto).toList();
  }
}
