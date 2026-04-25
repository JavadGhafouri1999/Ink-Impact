package com.weblog.postservice.dto.request;

import jakarta.validation.constraints.Size;

public record PostUpdateRequestDto(
		
		@Size(min = 3, max = 200, message = "Title must be between 3 and 200 characters")
		String title,
		
		@Size(min = 10, max = 50000, message = "Content must be between 10 and 50000 characters")
		String content,
		
		Boolean published
) { }
