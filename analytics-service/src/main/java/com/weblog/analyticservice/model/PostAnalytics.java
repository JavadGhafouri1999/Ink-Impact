package com.weblog.analyticservice.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "post_analytics")
public class PostAnalytics {

  @Id
  private UUID          id;
  private String        postId;
  private String        title;
  private String        slug;
  private String        authorId;
  private List <String> tags;
  private Instant       createdAt;
  private LocalDateTime ingestedAt;
}
