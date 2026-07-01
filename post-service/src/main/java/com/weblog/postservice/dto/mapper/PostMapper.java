package com.weblog.postservice.dto.mapper;

import com.weblog.postservice.dto.request.PostCreateRequestDto;
import com.weblog.postservice.dto.response.PostResponseDto;
import com.weblog.postservice.dto.response.PostSummaryResponse;
import com.weblog.postservice.model.Post;
import com.weblog.postservice.model.PostSearchDocument;
import com.weblog.postservice.utils.SlugGenerator;

import java.time.LocalDateTime;
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
			post.getTags() == null
			? List.of()
			: List.copyOf(post.getTags()),
			post.getImages() == null
			? List.of()
			: List.copyOf(post.getImages()),
			post.getBannerImageUrl(),
			String.valueOf(post.getStatus()),
			post.isPremium(),
			post.getAuthorId(),
			post.getViewCount(),
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
			post.isPremium(),
			post.getCreatedAt(),
			post.getViewCount()
	);
  }

  public static Post toModel(
		  PostCreateRequestDto request,
		  UUID authorId,
		  String bannerImageUrl,
		  List <String> imageUrls
							) {
	Post post = new Post();

	post.setTitle(request.title());
	post.setContent(request.content());

	post.setSlug(
			SlugGenerator.generateSlug(
					request.title()
									  )
				);

	post.setAuthorId(authorId);

	post.setTags(
			request.tags() != null
			? List.copyOf(request.tags())
			: List.of()
				);

	post.setBannerImageUrl(bannerImageUrl);

	post.setImages(
			imageUrls != null
			? List.copyOf(imageUrls)
			: List.of()
				  );

	post.setPremium(request.isPremium());

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
			doc.getIsPremium(),
			LocalDateTime.parse(doc.getCreatedAt()),
			doc.getViewCount()
	);
  }
}
