package com.weblog.analyticservice.dto.response;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record PostAnalyticsDto(
		UUID id,
		String postId,
		String title,
		String slug,
		String authorId,
		List <String> tags,
		Instant createdAt,
		LocalDateTime ingestedAt
) {
}
