package com.weblog.analyticservice.controller;

import com.weblog.analyticservice.dto.response.CommentAnalyticsDto;
import com.weblog.analyticservice.dto.response.PostAnalyticsDto;
import com.weblog.analyticservice.service.DashboardAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/admin/analytics")
@RequiredArgsConstructor
public class DashboardAnalyticsController {

  private final DashboardAnalyticsService service;

  @GetMapping("/posts/total")
  public int totalPosts() {
	return service.totalPosts();
  }

  @GetMapping("/comments/total")
  public int totalComments() {
	return service.totalComments();
  }

  @GetMapping("/posts/latest")
  public List <PostAnalyticsDto> latestPosts(@RequestParam(defaultValue = "20") int limit) {
	return service.latestPosts(limit);
  }

  @GetMapping("/comments/latest")
  public List <CommentAnalyticsDto> latestComments(@RequestParam(defaultValue = "20") int limit) {
	return service.latestComments(limit);
  }

  @GetMapping("/posts/range")
  public List <PostAnalyticsDto> postsBetween(
		  @RequestParam Instant start,
		  @RequestParam Instant end
											 ) {
	return service.postsBetween(start, end);
  }

  @GetMapping("/comments/range")
  public List <CommentAnalyticsDto> commentsBetween(
		  @RequestParam Instant start,
		  @RequestParam Instant end
												   ) {
	return service.commentsBetween(start, end);
  }

  @GetMapping("/posts/per-day")
  public List <Object[]> postsPerDay(@RequestParam(defaultValue = "30") int days) {
	return service.postsPerDay(days);
  }

  @GetMapping("/comments/per-day")
  public List <Object[]> commentsPerDay(@RequestParam(defaultValue = "30") int days) {
	return service.commentsPerDay(days);
  }
}
