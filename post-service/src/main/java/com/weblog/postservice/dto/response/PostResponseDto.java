package com.weblog.postservice.dto.response;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public record PostResponseDto(
		UUID id,
		String title,
		String slug,
		String content,
		List <String> tags,
		List <String> images,
		String bannerImageUrl,
		String status,
		Boolean premium,
		UUID authorId,
		Long viewCount,
		LocalDateTime createdAt,
		LocalDateTime updatedAt) { }
