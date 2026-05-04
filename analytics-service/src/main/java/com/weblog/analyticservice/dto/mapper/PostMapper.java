package com.weblog.analyticservice.dto.mapper;

import com.weblog.analyticservice.dto.response.PostAnalyticsDto;
import com.weblog.analyticservice.model.PostAnalytics;

import java.util.List;

public final class PostMapper {
	
	private PostMapper() { }
	
	public static PostAnalyticsDto toDto(PostAnalytics post) {
		return new PostAnalyticsDto(
				post.getId(),
				post.getPostId(),
				post.getTitle(),
				post.getSlug(),
				post.getAuthorId(),
				post.getTags(),
				post.getCreatedAt(),
				post.getIngestedAt()
		);
	}
	
	public static List <PostAnalyticsDto> toDtoList(List <PostAnalytics> list) {
		return list.stream()
		           .map(PostMapper::toDto)
		           .toList();
	}
}
