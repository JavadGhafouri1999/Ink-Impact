package com.weblog.postservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record PostCreateRequestDto(

		@NotBlank(message = "Title is required")
		@Size(min = 3, max = 200)
		String title,

		@NotBlank(message = "Content is required")
		@Size(min = 10, max = 50000)
		String content,

		@Size(max = 10)
		List <
				@NotBlank
				@Size(min = 2, max = 30)
						String
				> tags
) { }
