package com.weblog.analyticservice.service;

import com.weblog.analyticservice.dto.mapper.CommentMapper;
import com.weblog.analyticservice.dto.mapper.PostMapper;
import com.weblog.analyticservice.dto.response.CommentAnalyticsDto;
import com.weblog.analyticservice.dto.response.DailyCommentCount;
import com.weblog.analyticservice.dto.response.DailyPostCount;
import com.weblog.analyticservice.dto.response.PostAnalyticsDto;
import com.weblog.analyticservice.repository.CommentAnalyticsRepository;
import com.weblog.analyticservice.repository.PostAnalyticsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardAnalyticsService {

  private final PostAnalyticsRepository    postRepo;
  private final CommentAnalyticsRepository commentRepo;


  public int totalPosts() {
	return postRepo.countTotalPost();
  }

  public int totalComments() {
	return commentRepo.countComments();
  }

  public List <PostAnalyticsDto> latestPosts(int limit) {
	return PostMapper.toDtoList(postRepo.findLatest(limit));
  }

  public List <CommentAnalyticsDto> latestComments(int limit) {
	return CommentMapper.toDtoList(commentRepo.findLatest(limit));
  }

  public List <PostAnalyticsDto> postsBetween(Instant start, Instant end) {
	return PostMapper.toDtoList(postRepo.findBetween(start, end));
  }

  public List <CommentAnalyticsDto> commentsBetween(Instant start, Instant end) {
	return CommentMapper.toDtoList(commentRepo.findBetween(start, end));
  }

  public List <DailyPostCount> postsPerDay(int days) {
	return postRepo.countPostsPerDay(days);
  }

  public List <DailyCommentCount> commentsPerDay(int days) {
	return commentRepo.countCommentsPerDay(days);
  }
}
