package com.weblog.analyticservice.controller;

import com.weblog.analyticservice.dto.response.CommentAnalyticsDto;
import com.weblog.analyticservice.dto.response.DailyCommentCount;
import com.weblog.analyticservice.dto.response.DailyPostCount;
import com.weblog.analyticservice.dto.response.PostAnalyticsDto;
import com.weblog.analyticservice.service.DashboardAnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/admin/analytics")
@RequiredArgsConstructor
@Tag(name = "Analytics", description = "Analytics APIs")
public class DashboardAnalyticsController {

  private final DashboardAnalyticsService service;

  @GetMapping("/posts/total")
  @Operation(summary = "Total posts")
  public ResponseEntity <Integer> totalPosts() {
	return ResponseEntity.ok(service.totalPosts());
  }

  @GetMapping("/comments/total")
  @Operation(summary = "Total comments")
  public ResponseEntity <Integer> totalComments() {
	return ResponseEntity.ok(service.totalComments());
  }

  @GetMapping("/posts/latest")
  @Operation(summary = "Latest posts")
  public ResponseEntity <List <PostAnalyticsDto>> latestPosts(
		  @RequestParam(defaultValue = "20") int limit
															 ) {
	return ResponseEntity.ok(service.latestPosts(limit));
  }

  @GetMapping("/comments/latest")
  @Operation(summary = "Latest comments")
  public ResponseEntity <List <CommentAnalyticsDto>> latestComments(
		  @RequestParam(defaultValue = "20") int limit
																   ) {
	return ResponseEntity.ok(service.latestComments(limit));
  }

  @GetMapping("/posts/range")
  @Operation(summary = "Posts between dates")
  public ResponseEntity <List <PostAnalyticsDto>> postsBetween(
		  @RequestParam Instant start,
		  @RequestParam Instant end
															  ) {
	return ResponseEntity.ok(service.postsBetween(start, end));
  }

  @GetMapping("/comments/range")
  @Operation(summary = "Comments between dates")
  public ResponseEntity <List <CommentAnalyticsDto>> commentsBetween(
		  @RequestParam Instant start,
		  @RequestParam Instant end
																	) {
	return ResponseEntity.ok(service.commentsBetween(start, end));
  }

  @GetMapping("/posts/per-day")
  @Operation(summary = "Posts per day")
  public ResponseEntity <List <DailyPostCount>> postsPerDay(
		  @RequestParam(defaultValue = "30") int days
														   ) {
	return ResponseEntity.ok(service.postsPerDay(days));
  }

  @GetMapping("/comments/per-day")
  @Operation(summary = "Comments per day")
  public ResponseEntity <List <DailyCommentCount>> commentsPerDay(
		  @RequestParam(defaultValue = "30") int days
																 ) {
	return ResponseEntity.ok(service.commentsPerDay(days));
  }
}
