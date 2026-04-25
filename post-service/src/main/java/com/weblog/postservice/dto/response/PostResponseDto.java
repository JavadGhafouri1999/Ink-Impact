package com.weblog.postservice.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record PostResponseDto(
		UUID id,
		String title,
		String slug,
		String content,      // ← full content
		boolean published,
		UUID authorId,
		LocalDateTime createdAt,
		LocalDateTime updatedAt) { }
