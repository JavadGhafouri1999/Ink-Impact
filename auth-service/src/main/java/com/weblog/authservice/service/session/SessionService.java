package com.weblog.authservice.service.session;

import com.weblog.authservice.dto.response.SessionResponse;
import com.weblog.authservice.model.Session;
import com.weblog.authservice.model.User;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.UUID;

public interface SessionService {

Session createSession(User user, HttpServletRequest request);

boolean validateSession(String refreshToken);

Session refreshSession(String oldRefreshToken);

void logout(String refreshToken);

List <SessionResponse> getUserSessions(UUID userId);
}
