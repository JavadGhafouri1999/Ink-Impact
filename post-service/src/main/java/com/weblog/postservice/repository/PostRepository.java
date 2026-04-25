package com.weblog.postservice.repository;

import com.weblog.postservice.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PostRepository extends JpaRepository <Post, UUID> {
	Optional <Post> findBySlug(String slug);
	
	Page <Post> findByAuthorId(UUID authorId, Pageable pageable);
	
	boolean existsByTitle(String title);
	
	// For pagination - only published posts
	Page <Post> findByPublishedTrue(Pageable pageable);
	
	// For admin - all posts (drafts + published)
	Page <Post> findAll(Pageable pageable);
}
