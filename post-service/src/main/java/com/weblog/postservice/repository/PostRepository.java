package com.weblog.postservice.repository;

import com.weblog.postservice.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface PostRepository extends JpaRepository <Post, UUID> {

  Optional <Post> findBySlugAndStatus(
		  String slug,
		  Post.PostStatus status);

  Page <Post> findByAuthorIdAndStatus(
		  UUID authorId,
		  Post.PostStatus status,
		  Pageable pageable);

  boolean existsByTitle(String title);

  boolean existsById(UUID postId);

  // For pagination - only published posts
  Page <Post> findByStatus(Post.PostStatus status, Pageable pageable);

  // For admin - all posts (drafts + published)
  Page <Post> findAll(Pageable pageable);

  @Query("""
		 SELECT p FROM post p
		 JOIN p.tags t
		 WHERE t = :tag
		 AND p.status = com.weblog.postservice.model.Post.PostStatus.PUBLISHED
		 """)
  Page <Post> findPublishedByTag(
		  @Param("tag") String tag,
		  Pageable pageable);
}
