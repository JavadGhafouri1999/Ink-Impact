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
import com.weblog.postservice.exception.DuplicateTitleException;
import com.weblog.postservice.exception.PostNotFoundException;
import com.weblog.postservice.exception.UnauthorizedException;
import com.weblog.postservice.kafka.KafkaProducer;
import com.weblog.postservice.model.Post;
import com.weblog.postservice.model.PostSearchDocument;
import com.weblog.postservice.repository.PostRepository;
import com.weblog.postservice.repository.PostSearchRepository;
import com.weblog.postservice.utils.SlugGenerator;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service // 1. Add this to make it a Spring Bean
@Slf4j
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

  private final PostRepository postRepo; // 2. Make it final for safety
  private final PostSearchRepository repo;
  private final ElasticsearchOperations operations;
  private final KafkaProducer kafkaProducer;

  public void indexPost(Post post) {
    PostSearchDocument doc = new PostSearchDocument();
    doc.setId(post.getId().toString());
    doc.setTitle(post.getTitle());
    doc.setContent(post.getContent());
    doc.setTags(post.getTags());
    doc.setCreatedAt(
        post.getCreatedAt() != null
            ? post.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant()
            : Instant.now());
    repo.save(doc);
  }

  private Post findPostAndVerifyOwnership(UUID postId, UUID authorId) {
    Post post = postRepo.findById(postId).orElseThrow(() -> new PostNotFoundException(postId));

    if (!post.getAuthorId().equals(authorId)) {
      throw new UnauthorizedException("You can only modify your own posts");
    }

    return post;
  }

  @Override
  @Transactional // Good for write operations
  public PostResponseDto create(PostCreateRequestDto request, UUID authorId, String role) {
    // 1. Authorization check
    if (!role.equals("AUTHOR") && !role.equals("ADMIN") && !role.equals("OWNER")) {
      throw new UnauthorizedException("You are not allowed to create posts");
    }

    try {
      Post post = postRepo.save(PostMapper.toModel(request, authorId));
      indexPost(post);

      // kafka-Event
      kafkaProducer.sendEvent(post);

      return PostMapper.toDto(post);
    } catch (DataIntegrityViolationException e) {
      throw new DuplicateTitleException(request.title());
    }
  }

  @Override
  @Transactional
  @CachePut(value = "posts", key = "#result.id")
  public PostResponseDto update(UUID id, UUID authorId, PostUpdateRequestDto request, String role) {

    if (!role.equals("AUTHOR") && !role.equals("ADMIN") && !role.equals("OWNER")) {
      throw new UnauthorizedException("You are not allowed to update posts");
    }

    Post post = findPostAndVerifyOwnership(id, authorId);

    if (request.title() != null) {
      // Only check duplicate if title actually changed
      if (!request.title().equals(post.getTitle()) && postRepo.existsByTitle(request.title())) {
        throw new DuplicateTitleException(request.title());
      }
      post.setTitle(request.title());
      post.setSlug(SlugGenerator.generateSlug(request.title()));
    }
    if (request.tags() != null) {
      post.setTags(request.tags());
    }
    if (request.content() != null) {
      post.setContent(request.content());
    }
    if (request.published() != null) {
      post.setPublished(request.published());
    }
    Post updated = postRepo.save(post);
    indexPost(updated);
    return PostMapper.toDto(updated);
  }

  @Override
  // Do NOT put @Transactional on simple reads
  public PagedResponse<PostSummaryResponse> getPosts(int page, int size, String sortBy) {
    Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).descending());
    Page<PostSummaryResponse> result =
        postRepo.findByPublishedTrue(pageable).map(PostMapper::toSummaryDto);

    return PagedResponse.from(result);
  }

  @Override
  @Transactional(readOnly = true) // Hibernate: "Don't bother tracking changes"
  @Cacheable(value = "posts", key = "#id")
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
  @Cacheable(value = "posts", key = "#slug")
  public PostResponseDto getBySlug(String slug) {
    log.debug("Cache miss - fetching post by slug from DB: {}", slug);
    Post post = postRepo.findBySlug(slug).orElseThrow(() -> new PostNotFoundException(slug));
    return PostMapper.toDto(post);
  }

  @Override
  @Transactional(readOnly = true)
  public PagedResponse<PostSummaryResponse> getByAuthorId(
      UUID authorId, int page, int size, String sortBy) {
    Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).descending());

    Page<Post> posts = postRepo.findByAuthorId(authorId, pageable);

    return PagedResponse.from(posts.map(PostMapper::toSummaryDto));
  }

  @Override
  @Transactional(readOnly = true)
  public PagedResponse<PostSummaryResponse> getByTag(String tag, int page, int size) {
    Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
    Page<Post> posts = postRepo.findByTag(tag, pageable);
    return PagedResponse.from(posts.map(PostMapper::toSummaryDto));
  }

  @Override
  @Transactional
  @CacheEvict(value = "posts", key = "#id")
  public void deletePost(UUID authorId, UUID id, String role) {

    if (!role.equals("AUTHOR") && !role.equals("ADMIN") && !role.equals("OWNER")) {
      throw new UnauthorizedException("You are not allowed to delete posts");
    }

    Post post = findPostAndVerifyOwnership(id, authorId);
    postRepo.delete(post);
  }

  public List<PostSearchDocument> search(String q) {
    var query =
        NativeQuery.builder()
            .withQuery(
                qb -> qb.multiMatch(m -> m.fields("title", "content").query(q).fuzziness("AUTO")))
            .build();
    var hits = operations.search(query, PostSearchDocument.class);

    return hits.stream().map(hit -> hit.getContent()).toList();
  }
}
