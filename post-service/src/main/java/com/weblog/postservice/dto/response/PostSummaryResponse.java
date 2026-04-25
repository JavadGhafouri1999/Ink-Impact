package com.weblog.postservice.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.UUID;

public record PostSummaryResponse(
		UUID id,
		String title,
		String slug,
		String excerpt,       // ← first 100 chars only
		UUID authorId,
		@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
		LocalDateTime createdAt) {
}
