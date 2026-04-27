package com.weblog.postservice.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record PostResponseDto(
		UUID id,
		String title,
		String slug,
		String content,
		List <String> tags,
		boolean published,
		UUID authorId,
		LocalDateTime createdAt,
		LocalDateTime updatedAt) { }
