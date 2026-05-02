package com.weblog.analyticservice.dto.response;

import java.time.Instant;
import java.time.LocalDateTime;

public record CommentAnalyticsDto(
		String commentId,
		String postId,
		String userId,
		Instant createdAt,
		LocalDateTime ingestedAt
) {
}
