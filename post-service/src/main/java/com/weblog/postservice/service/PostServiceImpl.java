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
import com.weblog.postservice.event.PostViewedEvent;
import com.weblog.postservice.exception.DuplicateTitleException;
import com.weblog.postservice.exception.FileStorageException;
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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

  private final PostRepository                 postRepo;
  private final ElasticsearchOperations        operations;
  private final ApplicationEventPublisher      eventPublisher;
  private final RedisTemplate <String, String> redisTemplate; // ← For tracking unique viewers
  private final FileStorageService             fileStorageService;

  // ── Redis key prefix for view tracking ──
  private static final String VIEWER_KEY_PREFIX = "post:%s:viewer:%s"; // ←
  // post:{postId}:viewer:{userId}
  private static final long   VIEWER_TTL_DAYS   = 30; // ← Track viewers for 30 days


  @Override
  @Transactional // Good for write operations
  public PostResponseDto create(
		  PostCreateRequestDto request,
		  MultipartFile bannerImage,
		  List <MultipartFile> images,
		  UUID authorId,
		  String role) {

	// 1. Authorization check
	if(!role.equals("AUTHOR") && !role.equals("ADMIN") && !role.equals("OWNER")) {
	  throw new UnauthorizedException("You are not allowed to create posts");
	}

	if(images != null && images.size() > 20) {
	  throw new FileStorageException("Maximum 20 images allowed");
	}

	try {

	  if(postRepo.existsByTitle(request.title())) {
		throw new DuplicateTitleException(request.title());
	  }

	  // Handle banner image upload
	  String bannerImageUrl = null;
	  if(bannerImage != null && !bannerImage.isEmpty()) {
		bannerImageUrl = fileStorageService.uploadFile(bannerImage, "banners");
	  }

	  // Handle content images upload
	  List <String> imageUrls = new ArrayList <>();
	  if(images != null && !images.isEmpty()) {
		for(MultipartFile image : images) {
		  String imageUrl = fileStorageService.uploadFile(image, "content");
		  imageUrls.add(imageUrl);
		}
	  }

	  // Create request with uploaded file URLs
	  Post post = PostMapper.toModel(request, authorId, bannerImageUrl, imageUrls);
	  Post savedPost = postRepo.save(post);

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
  @Transactional(readOnly = true)
  public boolean existsById(UUID id) {
	return postRepo.existsById(id);
  }

  /**
   * GET post by ID with view tracking for authenticated users.
   * Tracks unique views in Redis and increments viewCount.
   *
   * @param id Post UUID
   * @param userId Optional UUID of viewing user (from X-User-Id header)
   * @return Full PostResponseDto with current viewCount
   */
  @Override
  @Transactional
  @Cacheable(value = "postsById", key = "#id")
  public PostResponseDto getById(UUID id, UUID userId, String role) {
	log.debug("Fetching post {} with view tracking for user {}", id, userId);

	Post post = postRepo.findByIdAndStatus(id, Post.PostStatus.PUBLISHED)
						.orElseThrow(() -> new PostNotFoundException(id));

	// ── Track view asynchronously to avoid blocking response ──
	trackUniqueView(id, userId);

	return PostMapper.toDto(post);
  }

  /**
   * GET post by slug with view tracking for authenticated users.
   * Tracks unique views in Redis and increments viewCount.
   *
   * @param slug Post URL-friendly slug
   * @param userId Optional UUID of viewing user (from X-User-Id header)
   * @return Full PostResponseDto with current viewCount
   */
  @Override
  @Transactional
  @Cacheable(value = "postsBySlug", key = "#slug")
  public PostResponseDto getBySlug(String slug, UUID userId, String role) {
	log.debug("Fetching post by slug {} with view tracking for user {}", slug, userId);

	Post post = postRepo.findBySlugAndStatus(slug, Post.PostStatus.PUBLISHED)
						.orElseThrow(() -> new PostNotFoundException(slug));

	// ── Track view asynchronously to avoid blocking response ──
	trackUniqueView(post.getId(), userId);

	return PostMapper.toDto(post);
  }

  @Override
  @Transactional(readOnly = true)
  public PagedResponse <PostSummaryResponse> getByAuthorId(
		  UUID authorId, UUID requesterId, int page, int size, String sortBy, String role) {

	Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).descending());

	boolean isAdmin =
			"ADMIN".equals(role) || "OWNER".equals(role);

	boolean isSameAuthor =
			requesterId != null &&
			requesterId.equals(authorId);

	Page <Post> posts;

	if(isAdmin || isSameAuthor) {
	  posts = postRepo.findByAuthorId(authorId, pageable);
	}
	else {
	  posts = postRepo.findByAuthorIdAndStatus(
			  authorId,
			  Post.PostStatus.PUBLISHED,
			  pageable
											  );
	}

	return PagedResponse.from(
			posts.map(PostMapper::toSummaryDto)
							 );
  }

  @Override
  @Transactional(readOnly = true)
  public PagedResponse <PostSummaryResponse> getByTag(String tag, int page, int size, String role) {
	Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
	Page <Post> posts = postRepo.findPublishedByTag(tag, pageable);
	return PagedResponse.from(posts.map(PostMapper::toSummaryDto));
  }

  @Override
  @Transactional(readOnly = true)
  public long postsCountByAuthor(UUID userId) {
	return postRepo.countByAuthorId(userId);
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

	// ── DELETE BANNER IMAGE IF EXISTS ──
	if(post.getBannerImageUrl() != null && !post.getBannerImageUrl().isEmpty()) {
	  String bannerFilename = extractFilename(post.getBannerImageUrl());
	  fileStorageService.deletePostImage(bannerFilename);
	}

	// ── DELETE ALL INLINE IMAGES ──
	if(post.getImages() != null && !post.getImages().isEmpty()) {
	  for(String imageUrl : post.getImages()) {
		String filename = extractFilename(imageUrl);
		fileStorageService.deletePostImage(filename);
	  }
	}

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


  private Post findPostAndVerifyOwnership(UUID postId, UUID authorId, String role) {
	Post post = postRepo.findById(postId)
						.orElseThrow(() -> new PostNotFoundException(postId));

	boolean isAdmin = role.equals("ADMIN") || role.equals("OWNER");

	if(!isAdmin && !post.getAuthorId().equals(authorId)) {
	  throw new UnauthorizedException("You can only modify your own posts");
	}

	return post;
  }

  private void validateAccess(Post post, UUID userId, String role) {
	boolean isAdmin =
			"ADMIN".equals(role) || "OWNER".equals(role);

	boolean isAuthor =
			post.getAuthorId().equals(userId);

	boolean canAccess =
			post.getStatus() == Post.PostStatus.PUBLISHED
			|| isAdmin
			|| isAuthor;

	if(!canAccess) {
	  throw new UnauthorizedException("You cannot access this post");
	}
  }

  private void trackUniqueView(UUID postId, UUID userId) {
	if(userId == null) { return; }

	String redisKey = String.format(VIEWER_KEY_PREFIX, postId, userId);

	Boolean isNewViewer = redisTemplate.opsForValue().setIfAbsent(
			redisKey,
			"1",
			Duration.ofDays(VIEWER_TTL_DAYS)
																 );
	// ── If new viewer: increment counter and publish event ──
	if(Boolean.TRUE.equals(isNewViewer)) {
	  // Increment viewCount in database efficiently
	  postRepo.incrementViewCount(postId);

	  // Publish event for analytics service to consume
	  eventPublisher.publishEvent(new PostViewedEvent(postId, userId));

	  log.debug("Post {} viewed by user {}: viewCount incremented", postId, userId);
	}
  }

  private String extractFilename(String url) {
	if(url == null || url.isEmpty()) {
	  return "";
	}
	int lastSlash = url.lastIndexOf('/');
	return lastSlash >= 0 ? url.substring(lastSlash + 1) : url;
  }

}
