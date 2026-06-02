package com.weblog.commentservice.repository;

import com.weblog.commentservice.model.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CommentRepository extends JpaRepository <Comment, UUID> {

  Page <Comment> findByPostId(UUID postId, Pageable pageable);

  long countByPostId(UUID postId);

  void deleteByPostId(UUID postId);

  void deleteAllByUserId(UUID userId);
}
