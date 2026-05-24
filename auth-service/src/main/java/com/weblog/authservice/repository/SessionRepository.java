package com.weblog.authservice.repository;

import com.weblog.authservice.model.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SessionRepository extends JpaRepository <Session, UUID> {

Optional <Session> findByIdAndActive(UUID id, boolean active);

Optional <Session> findById(UUID id);

List <Session> findByUserIdAndActive(UUID userId, boolean active);

@Query("UPDATE Session s SET s.active = false, s.revokedAt = CURRENT_TIMESTAMP WHERE s.user.id = :userId " +
	   "AND s.active = true")
@Modifying
void deactivateAllUserSessions(UUID userId);
}
