package com.weblog.postservice.controller;

import com.weblog.postservice.dto.request.PostCreateRequestDto;
import com.weblog.postservice.dto.request.PostUpdateRequestDto;
import com.weblog.postservice.dto.response.PagedResponse;
import com.weblog.postservice.dto.response.PostResponseDto;
import com.weblog.postservice.dto.response.PostSummaryResponse;
import com.weblog.postservice.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
@Tag(name = "Post", description = "This is the api for managing posts")
public class PostController {

  private final PostService postService;

  @GetMapping("/admin")
  @Operation(
		  summary = "Get all posts - Admins",
		  description = "list of all posts with optional sorting - even not published ones")
  public ResponseEntity <PagedResponse <PostSummaryResponse>> getAllPosts(
		  @RequestParam(defaultValue = "0") int page,
		  @RequestParam(defaultValue = "10") int size,
		  @RequestParam(defaultValue = "createdAt") String sortBy,
		  @RequestHeader("X-User-Role") String role) {
	return ResponseEntity.ok(postService.getAllPosts(role, page, size, sortBy));
  }

  @GetMapping
  @Operation(
		  summary = "Get published posts",
		  description = "Retrieve a paginated list of published posts with optional sorting")
  public ResponseEntity <PagedResponse <PostSummaryResponse>> getPublishedPosts(
		  @RequestParam(defaultValue = "0") int page,
		  @RequestParam(defaultValue = "10") int size,
		  @RequestParam(defaultValue = "createdAt") String sortBy) {
	return ResponseEntity.ok(postService.getPublishedPosts(page, size, sortBy));
  }

  @GetMapping("/search")
  public PagedResponse <PostSummaryResponse> search(
		  @RequestParam String q,
		  @RequestParam(defaultValue = "0") int page,
		  @RequestParam(defaultValue = "10") int size) {
	return postService.search(q, page, size);
  }

  @GetMapping("/{id}")
  @Operation(
		  summary = "Get post by ID",
		  description = "Retrieve a specific post by its unique identifier (UUID)")
  public ResponseEntity <PostResponseDto> getById
		  (@PathVariable UUID id,
		   @RequestHeader(value = "X-User-Id", required = false) String userId,
		   @RequestHeader(value = "X-User-Role", required = false) String role
		  ) {

	// ── Convert userId string to UUID, pass null if not authenticated ──
	UUID userIdUuid = userId != null ? UUID.fromString(userId) : null;
	return ResponseEntity.ok(postService.getById(id, userIdUuid, role));
  }

  @GetMapping("/slug/{slug}")
  @Operation(
		  summary = "Get post by slug",
		  description = "Retrieve a specific post by its URL-friendly slug")
  public ResponseEntity <PostResponseDto> getBySlug
		  (@PathVariable String slug,
		   @RequestHeader(value = "X-User-Id", required = false) String userId,
		   @RequestHeader(value = "X-User-Role", required = false) String role
		  ) {

	UUID userIdUuid = userId != null ? UUID.fromString(userId) : null;
	return ResponseEntity.ok(postService.getBySlug(slug, userIdUuid, role));
  }

  @GetMapping("/author/{authorId}")
  @Operation(
		  summary = "Get posts by author",
		  description = "Retrieve a paginated list of all posts created by a specific author")
  public ResponseEntity <PagedResponse <PostSummaryResponse>> getByAuthor(
		  @PathVariable UUID authorId,
		  @RequestParam(defaultValue = "0") int page,
		  @RequestParam(defaultValue = "10") int size,
		  @RequestParam(defaultValue = "createdAt") String sortBy,
		  @RequestHeader(value = "X-User-Id", required = false) String userId,
		  @RequestHeader(value = "X-User-Role", required = false) String role) {

	UUID requesterId =
			userId != null ? UUID.fromString(userId) : null;

	return ResponseEntity.ok(
			postService.getByAuthorId(
					authorId,
					requesterId,
					page,
					size,
					sortBy,
					role)
							);
  }

  @GetMapping("/tag/{tag}")
  @Operation(
		  summary = "Get posts by tag",
		  description = "Retrieve a paginated list of posts filtered by a specific tag")
  public ResponseEntity <PagedResponse <PostSummaryResponse>> getByTag(
		  @PathVariable String tag,
		  @RequestParam(defaultValue = "0") int page,
		  @RequestParam(defaultValue = "10") int size,
		  @RequestHeader(value = "X-User-Role", required = false) String role) {

	return ResponseEntity.ok(postService.getByTag(tag, page, size, role));
  }


  @PostMapping(
		  consumes = MediaType.MULTIPART_FORM_DATA_VALUE
  )
  @Operation(
		  summary = "Create a new post",
		  description = "Create a new post with the provided details")
  public ResponseEntity <PostResponseDto> create(
		  @Valid @RequestPart("post") PostCreateRequestDto postDto,
		  @RequestPart(value = "bannerImage", required = false) MultipartFile bannerImage,
		  @RequestPart(value = "images", required = false) List <MultipartFile> images,
		  @RequestHeader("X-User-Id") String userId,
		  @RequestHeader("X-User-Role") String role
												) {

	UUID authorId = UUID.fromString(userId);
	return ResponseEntity.ok(
			postService.create(postDto, bannerImage, images, authorId, role)
							);
  }

  @PatchMapping("/{id}")
  @Operation(
		  summary = "Update an existing post",
		  description = "Update a specific post by its ID with partial or complete information")
  public ResponseEntity <PostResponseDto> update(
		  @PathVariable UUID id,
		  @Valid @RequestBody PostUpdateRequestDto postDto,
		  @RequestHeader("X-User-Id") String userId,
		  @RequestHeader("X-User-Role") String role) {
	UUID authorId = UUID.fromString(userId);
	return ResponseEntity.ok(postService.update(id, authorId, postDto, role));
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete a post", description = "Delete a specific post by its ID")
  public ResponseEntity <String> delete(
		  @PathVariable UUID id,
		  @RequestHeader("X-User-Id") String userId,
		  @RequestHeader("X-User-Role") String role) {
	UUID authorId = UUID.fromString(userId);
	postService.deletePost(authorId, id, role);
	return ResponseEntity.ok().body("Post with ID " + id + " was deleted");
  }
}
