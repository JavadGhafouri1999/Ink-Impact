package com.weblog.authservice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "sessions")
@Getter
@Setter
@NoArgsConstructor
public class Session {

@Id
@GeneratedValue(strategy = GenerationType.UUID)
private UUID id;

@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "user_id", nullable = false)
private User user;

private String userAgent;

private String ipAddress;

@Column(nullable = false)
private boolean active;

@CreationTimestamp
private Instant createdAt;

private Instant expiresAt;

private Instant revokedAt;

public Session(boolean active, Instant expiresAt, String ipAddress, User user, String userAgent) {
  this.active = active;
  this.expiresAt = expiresAt;
  this.ipAddress = ipAddress;
  this.user = user;
  this.userAgent = userAgent;
}
}
