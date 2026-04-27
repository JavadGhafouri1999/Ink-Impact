package com.weblog.commentservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity(name = "comment")
@Getter
@Setter
@NoArgsConstructor
public class Comment {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;
	
	@NotNull
	@Column(nullable = false)
	private UUID userId;
	
	@NotNull
	@Column(nullable = false)
	private UUID postId;
	
	@NotBlank
	@Column(nullable = false, length = 120)
	private String content;
	
	@Column(name = "isPublished")
	private boolean isPublished = false;
	
	@CreationTimestamp
	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt;
	
	@UpdateTimestamp
	@Column(name = "updated_at")
	private LocalDateTime updatedAt;
	
	public Comment(UUID userId, String content, LocalDateTime createdAt, UUID postId,
	               boolean isPublished) {
		this.userId      = userId;
		this.content     = content;
		this.createdAt   = createdAt;
		this.postId      = postId;
		this.isPublished = isPublished;
	}
}
