package com.weblog.analyticservice.dto.mapper;

import com.weblog.analyticservice.dto.response.CommentAnalyticsDto;
import com.weblog.analyticservice.model.CommentAnalytics;

import java.util.List;

public final class CommentMapper {

  private CommentMapper() { }

  static public CommentAnalyticsDto toDto(CommentAnalytics comment) {
	return new CommentAnalyticsDto(
			comment.getCommentId(),
			comment.getPostId(),
			comment.getUserId(),
			comment.getCreatedAt(),
			comment.getIngestedAt()
	);
  }

  public static List <CommentAnalyticsDto> toDtoList(List <CommentAnalytics> list) {
	return list.stream()
			   .map(CommentMapper::toDto)
			   .toList();
  }

}
