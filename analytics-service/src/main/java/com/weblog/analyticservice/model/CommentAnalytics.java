package com.weblog.analyticservice.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "comment_analytics")
public class CommentAnalytics {

  @Id
  private UUID          id;
  private String        commentId;
  private String        postId;
  private String        userId;
  private Instant       createdAt;
  private LocalDateTime ingestedAt;
}
