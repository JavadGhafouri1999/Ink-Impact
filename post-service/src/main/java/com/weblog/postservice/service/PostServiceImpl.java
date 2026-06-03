/**
 * Purpose: Actual logic. Defines HOW methods work.
 *
 * <p>Used by: Spring injects it wherever PostService is requested
 */
package com.weblog.postservice.service;

import com.weblog.postservice.dto.mapper.PostMapper;
import com.weblog.postservice.dto.request.PostCreateRequestDto;
import com.weblog.postservice.dto.request.PostUpdateRequestDto;
import com.weblog.postservice.dto.response.PagedResponse;
import com.weblog.postservice.dto.response.PostResponseDto;
import com.weblog.postservice.dto.response.PostSummaryResponse;
import com.weblog.postservice.event.PostCreatedEvent;
import com.weblog.postservice.event.PostDeletedEvent;
import com.weblog.postservice.event.PostUpdatedEvent;
import com.weblog.postservice.exception.DuplicateTitleException;
import com.weblog.postservice.exception.PostNotFoundException;
import com.weblog.postservice.exception.UnauthorizedException;
import com.weblog.postservice.model.Post;
import com.weblog.postservice.model.PostSearchDocument;
import com.weblog.postservice.repository.PostRepository;
import com.weblog.postservice.utils.SlugGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

  private final PostRepository            postRepo;
  private final ElasticsearchOperations   operations;
  private final ApplicationEventPublisher eventPublisher;

  private Post findPostAndVerifyOwnership(UUID postId, UUID authorId, String role) {
	Post post = postRepo.findById(postId)
						.orElseThrow(() -> new PostNotFoundException(postId));

	boolean isAdmin = role.equals("ADMIN") || role.equals("OWNER");

	if(!isAdmin && !post.getAuthorId().equals(authorId)) {
	  throw new UnauthorizedException("You can only modify your own posts");
	}

	return post;
  }

  @Override
  @Transactional // Good for write operations
  public PostResponseDto create(PostCreateRequestDto request, UUID authorId, String role) {
	// 1. Authorization check
	if(!role.equals("AUTHOR") && !role.equals("ADMIN") && !role.equals("OWNER")) {
	  throw new UnauthorizedException("You are not allowed to create posts");
	}

	try {
	  Post post = PostMapper.toModel(request, authorId);
	  Post savedPost = postRepo.save(post);

	  // publish event AFTER DB save
	  eventPublisher.publishEvent(new PostCreatedEvent(savedPost));

	  return PostMapper.toDto(savedPost);
	} catch(DataIntegrityViolationException e) {
	  throw new DuplicateTitleException(request.title());
	}
  }

  @Override
  @Transactional
  @Caching(evict = {
		  @CacheEvict(value = "postsById", key = "#id"),
		  @CacheEvict(value = "postsBySlug", allEntries = true) // Safer if slug could change
  })
  public PostResponseDto update(UUID id, UUID authorId, PostUpdateRequestDto request, String role) {

	if(!role.equals("AUTHOR") && !role.equals("ADMIN") && !role.equals("OWNER")) {
	  throw new UnauthorizedException("You are not allowed to update posts");
	}

	Post post = findPostAndVerifyOwnership(id, authorId, role);

	if(request.title() != null) {
	  // Only check duplicate if title actually changed
	  if(!request.title().equals(post.getTitle()) && postRepo.existsByTitle(request.title())) {
		throw new DuplicateTitleException(request.title());
	  }
	  post.setTitle(request.title());
	  post.setSlug(SlugGenerator.generateSlug(request.title()));
	}
	if(request.tags() != null) {
	  post.setTags(request.tags());
	}
	if(request.content() != null) {
	  post.setContent(request.content());
	}
	if(request.bannerImageUrl() != null) {
	  post.setBannerImageUrl(request.bannerImageUrl());
	}
	if(request.images() != null) {
	  post.setImages(request.images());
	}
	if((role.equals("ADMIN") || role.equals("OWNER")) && request.status() != null) {
	  post.setStatus(request.status());
	}
	Post updated = postRepo.saveAndFlush(post);

	eventPublisher.publishEvent(new PostUpdatedEvent(updated));

	return PostMapper.toDto(updated);
  }

  @Override
  // Do NOT put @Transactional on simple reads
  public PagedResponse <PostSummaryResponse> getPublishedPosts(int page, int size, String sortBy) {

	Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).descending());
	Page <PostSummaryResponse> result =
			postRepo.findByStatus(Post.PostStatus.PUBLISHED, pageable).map(PostMapper::toSummaryDto);
	return PagedResponse.from(result);
  }

  @Override
  public PagedResponse <PostSummaryResponse> getAllPosts(String role, int page, int size, String sortBy) {

	if(!role.equals("ADMIN") && !role.equals("OWNER")) {
	  throw new UnauthorizedException("You are not allowed to update posts");
	}

	Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).descending());
	Page <PostSummaryResponse> result =
			postRepo.findAll(pageable).map(PostMapper::toSummaryDto);
	return PagedResponse.from(result);
  }

  @Override
  @Transactional(readOnly = true) // Hibernate: "Don't bother tracking changes"
  @Cacheable(value = "postsById", key = "#id")
  public PostResponseDto getById(UUID id) {
	log.debug("Cache miss - request send to DB ID: {}", id);
	Post post = postRepo.findById(id).orElseThrow(() -> new PostNotFoundException(id));
	return PostMapper.toDto(post); // Nothing gets saved, just reading
  }

  @Override
  @Transactional(readOnly = true)
  public boolean existsById(UUID id) {
	return postRepo.existsById(id);
  }

  @Override
  @Transactional(readOnly = true)
  @Cacheable(value = "postsBySlug", key = "#slug")
  public PostResponseDto getBySlug(String slug) {
	log.debug("Cache miss - fetching post by slug from DB: {}", slug);
	Post post = postRepo.findBySlugAndStatus(slug, Post.PostStatus.PUBLISHED)
						.orElseThrow(() -> new PostNotFoundException(slug));
	return PostMapper.toDto(post);
  }

  @Override
  @Transactional(readOnly = true)
  public PagedResponse <PostSummaryResponse> getByAuthorId(
		  UUID authorId, int page, int size, String sortBy) {
	Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).descending());

	Page <Post> posts = postRepo.findByAuthorIdAndStatus(authorId, Post.PostStatus.PUBLISHED, pageable);

	return PagedResponse.from(posts.map(PostMapper::toSummaryDto));
  }

  @Override
  @Transactional(readOnly = true)
  public PagedResponse <PostSummaryResponse> getByTag(String tag, int page, int size) {
	Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
	Page <Post> posts = postRepo.findPublishedByTag(tag, pageable);
	return PagedResponse.from(posts.map(PostMapper::toSummaryDto));
  }

  @Override
  @Transactional
  @Caching(evict = {
		  @CacheEvict(value = "postsById", key = "#id"),
		  @CacheEvict(value = "postsBySlug", allEntries = true)
  })
  public void deletePost(UUID authorId, UUID id, String role) {

	if(!role.equals("AUTHOR") && !role.equals("ADMIN") && !role.equals("OWNER")) {
	  throw new UnauthorizedException("You are not allowed to delete posts");
	}

	Post post = findPostAndVerifyOwnership(id, authorId, role);
	postRepo.delete(post);

	eventPublisher.publishEvent(new PostDeletedEvent(id));
  }

  @Override
  @Transactional(readOnly = true)
  public PagedResponse <PostSummaryResponse> search(String q, int page, int size) {
	var pageable = PageRequest.of(page, size);

	var query = NativeQuery.builder()
						   .withQuery(
								   qb -> qb
										   .multiMatch(
												   m -> m
														   .fields("title^5", "content^2")
														   .query(q)
														   .fuzziness("AUTO")
													  )
									 )
						   .withPageable(pageable)
						   .build();

	var searchHits =
			operations.search(query, PostSearchDocument.class);

	List <PostSummaryResponse> content = searchHits
			.stream()
			.map(hit -> PostMapper.toSummaryDtoFromSearch(hit.getContent()))
			.toList();

	long totalElements = searchHits.getTotalHits();
	int totalPages = (int) Math.ceil((double) totalElements / size);

	return new PagedResponse <>(
			content,
			page,
			size,
			totalElements,
			totalPages,
			page == 0,
			page >= totalPages - 1
	);
  }
}
