package com.weblog.authservice.repository;

import com.weblog.authservice.model.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SessionRepository extends JpaRepository <Session, UUID> {
  
  Optional <Session> findByRefreshTokenAndActive(String refreshToken, boolean active);
  
  Optional <Session> findByRefreshToken(String refreshToken);
  
  List <Session> findByUserIdAndActive(UUID userId, boolean active);
  
  
}
