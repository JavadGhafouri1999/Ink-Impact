package com.weblog.postservice.dto.mapper;

import com.weblog.postservice.dto.request.PostCreateRequestDto;
import com.weblog.postservice.dto.response.PostResponseDto;
import com.weblog.postservice.dto.response.PostSummaryResponse;
import com.weblog.postservice.model.Post;
import com.weblog.postservice.utils.SlugGenerator;

import java.util.UUID;

public class PostMapper {
	
	private PostMapper() { }
	
	public static PostResponseDto toDto(Post post) {
		return new PostResponseDto(
				post.getId(),
				post.getTitle(),
				post.getSlug(),
				post.getContent(),
				post.isPublished(),
				post.getAuthorId(),
				post.getCreatedAt(),
				post.getUpdatedAt()
		);
	}
	
	public static PostSummaryResponse toSummaryDto(Post post) {
		String excerpt = post.getContent() != null && post.getContent().length() > 100
		                 ? post.getContent().substring(0, 100) + "..."
		                 : post.getContent();
		
		return new PostSummaryResponse(
				post.getId(),
				post.getTitle(),
				post.getSlug(),
				excerpt,
				post.getAuthorId(),
				post.getCreatedAt()
		);
	}
	
	public static Post toModel(PostCreateRequestDto request, UUID authorId) {
		Post post = new Post();
		post.setTitle(request.title());
		post.setContent(request.content());
		post.setSlug(SlugGenerator.generateSlug(request.title()));
		post.setAuthorId(authorId);
		post.setPublished(false);
		
		return post;
	}
}
