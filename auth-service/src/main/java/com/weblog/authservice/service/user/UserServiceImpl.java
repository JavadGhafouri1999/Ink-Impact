package com.weblog.authservice.service.user;

import com.weblog.authservice.dto.mapper.UserMapper;
import com.weblog.authservice.dto.request.LoginRequest;
import com.weblog.authservice.dto.request.RegisterRequest;
import com.weblog.authservice.dto.response.AuthResponse;
import com.weblog.authservice.dto.response.UserResponse;
import com.weblog.authservice.exception.BadRequestException;
import com.weblog.authservice.exception.UnauthorizedException;
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

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

private final UserRepository  userRepo;
private final PasswordEncoder passwordEncoder;
private final JwtService      jwtService;
private final SessionService  sessionService;

@Override
@Transactional
public UserResponse register(RegisterRequest request) {

  String email = request.email().toLowerCase().trim();
  String phone = request.phone().trim();
  String username = request.username().trim();

  // Single DB hit for all unique fields
  List <User> conflicts = userRepo.findConflicts(email, username, phone);

  for(User u : conflicts) {
	if(u.getEmail().equals(email)) { throw new BadRequestException("Email already taken"); }

	if(u.getUsername().equals(username)) { throw new BadRequestException("Username already taken"); }

	if(u.getPhone().equals(phone)) { throw new BadRequestException("Phone already taken"); }
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
}
