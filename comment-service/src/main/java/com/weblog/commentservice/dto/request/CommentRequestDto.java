package com.weblog.commentservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CommentRequestDto(
		@NotBlank(message = "Comment can not be empty")
		@Size(min = 3, max = 120, message = "Content must be between 3 and 120 characters")
		String content,

		@NotNull(message = "Post ID is required")
		UUID postId
) {
}
