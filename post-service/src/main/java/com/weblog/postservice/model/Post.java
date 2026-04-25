package com.weblog.postservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity(name = "post")
public class Post {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;
	
	@Column(name = "author_id", nullable = false)
	private UUID authorId;
	
	@NotBlank(message = "Title is required")
	@Column(nullable = false, unique = true)
	private String title;
	
	@NotBlank(message = "Content is required")
	@Column(nullable = false, columnDefinition = "TEXT")
	private String content;
	
	private String slug;
	
	@Column(name = "published")
	private boolean published = false;
	
	@CreationTimestamp
	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt;
	
	@UpdateTimestamp
	@Column(name = "updated_at")
	private LocalDateTime updatedAt;
	
	public Post(UUID authorId, String content, LocalDateTime createdAt, boolean published, String slug,
	            String title, LocalDateTime updatedAt) {
		this.authorId  = authorId;
		this.title     = title;
		this.slug      = slug;
		this.content   = content;
		this.published = published;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}
}
