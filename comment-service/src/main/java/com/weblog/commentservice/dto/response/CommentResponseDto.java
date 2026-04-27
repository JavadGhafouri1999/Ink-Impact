package com.weblog.commentservice.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record CommentResponseDto(
		UUID id,
		UUID postId,
		UUID userId,
		String content,
		boolean published,
		LocalDateTime createdAt,
		LocalDateTime updatedAt
) {
}
