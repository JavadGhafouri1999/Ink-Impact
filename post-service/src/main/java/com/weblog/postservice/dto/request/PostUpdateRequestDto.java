package com.weblog.postservice.dto.request;

import com.weblog.postservice.model.Post;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record PostUpdateRequestDto(
		@Size(min = 3, max = 200, message = "Title must be between 3 and 200 characters")
		String title,

		@Size(min = 10, max = 50000, message = "Content must be between 10 and 50000 characters")
		String content,

		@Size(max = 10, message = "Maximum 10 tags allowed")
		List <@NotBlank(message = "Tag cannot be empty")
		@Size(min = 2, max = 30, message = "Tag must be between 2 and 30 characters") String> tags,

		String bannerImageUrl,

		@Size(max = 20, message = "Maximum 20 images allowed")
		List <@NotBlank String> images,

		Post.PostStatus status
) { }
