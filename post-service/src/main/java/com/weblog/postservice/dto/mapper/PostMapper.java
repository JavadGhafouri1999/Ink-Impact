package com.weblog.postservice.dto.mapper;

import com.weblog.postservice.dto.request.PostCreateRequestDto;
import com.weblog.postservice.dto.response.PostResponseDto;
import com.weblog.postservice.dto.response.PostSummaryResponse;
import com.weblog.postservice.model.Post;
import com.weblog.postservice.model.PostSearchDocument;
import com.weblog.postservice.utils.SlugGenerator;

import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

public final class PostMapper {

  private PostMapper() { }

  public static PostResponseDto toDto(Post post) {
	return new PostResponseDto(
			post.getId(),
			post.getTitle(),
			post.getSlug(),
			post.getContent(),
			List.copyOf(post.getTags()),
			List.copyOf(post.getImages()),
			post.getBannerImageUrl(),
			String.valueOf(post.getStatus()),
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
			post.getTags(),
			post.getBannerImageUrl(),
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

	post.setTags(request.tags() != null ? request.tags() : List.of());

	post.setBannerImageUrl(request.bannerImageUrl());

	post.setImages(request.images() != null ? request.images() : List.of());

	post.setStatus(Post.PostStatus.DRAFT);

	return post;
  }

  public static PostSummaryResponse toSummaryDtoFromSearch(PostSearchDocument doc) {
	String excerpt = doc.getContent() != null && doc.getContent().length() > 100
					 ? doc.getContent().substring(0, 100) + "..."
					 : doc.getContent();

	return new PostSummaryResponse(
			UUID.fromString(doc.getId()),
			doc.getTitle(),
			doc.getSlug(),
			excerpt,
			doc.getTags(),
			doc.getBannerImageUrl(),
			UUID.fromString(doc.getAuthorId()),
			doc.getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDateTime()
	);
  }
}
