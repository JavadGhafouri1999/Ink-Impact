package com.weblog.postservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "post_tags", joinColumns = @JoinColumn(name = "post_id"))
  @Column(name = "tag")
  private List <String> tags = new ArrayList <>();

  // ── STATUS CONTROL (IMPORTANT FIX) ──
  @Enumerated(EnumType.STRING)
  private PostStatus status = PostStatus.DRAFT;

  // ── BANNER IMAGE (1:1 OR HERO IMAGE) ──
  private String bannerImageUrl;

  // ── CONTENT IMAGES (INLINE GALLERY) ──
  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "post_images", joinColumns = @JoinColumn(name = "post_id"))
  @Column(name = "image_url")
  private List <String> images = new ArrayList <>();

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  public Post(UUID authorId, String bannerImageUrl, String content, List <String> images, String slug,
			  PostStatus status, List <String> tags, String title) {
	this.authorId = authorId;
	this.bannerImageUrl = bannerImageUrl;
	this.content = content;
	this.images = images;
	this.slug = slug;
	this.status = status;
	this.tags = tags;
	this.title = title;
  }

  public enum PostStatus {
	DRAFT,
	PENDING_REVIEW,
	PUBLISHED,
	ARCHIVED
  }
}
