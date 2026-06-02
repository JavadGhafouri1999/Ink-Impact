package com.weblog.analyticservice.model;

import lombok.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentAnalytics {

  private UUID          id;
  private String        commentId;
  private String        postId;
  private String        userId;
  private Instant       createdAt;
  private LocalDateTime ingestedAt;
}
