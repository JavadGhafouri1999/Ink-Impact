package com.weblog.authservice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false, unique = true)
  private String username;

  @Column(nullable = false, unique = true)
  private String email;

  @Column(nullable = false)
  private String password;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private Role role;

  @Column(nullable = false, unique = true)
  private String phone;

  private String avatarUrl;

  @CreationTimestamp
  private Instant createdAt;

  @UpdateTimestamp
  private Instant updatedAt;

  public User(
		  String avatarUrl, String email, String password, String phone, Role role, String username) {
	this.avatarUrl = avatarUrl;
	this.email = email;
	this.password = password;
	this.phone = phone;
	this.role = role;
	this.username = username;
  }

  public enum Role {
	OWNER,
	ADMIN,
	SPECIAL,
	AUTHOR,
	USER
  }
}
