package com.weblog.postservice.controller;

import com.weblog.postservice.dto.request.PostCreateRequestDto;
import com.weblog.postservice.dto.request.PostUpdateRequestDto;
import com.weblog.postservice.dto.response.PagedResponse;
import com.weblog.postservice.dto.response.PostResponseDto;
import com.weblog.postservice.dto.response.PostSummaryResponse;
import com.weblog.postservice.service.PostServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/posts")
public class PostController {
	private final PostServiceImpl postService;
	
	public PostController(PostServiceImpl postService) {
		this.postService = postService;
	}
	
	@GetMapping
	public ResponseEntity <PagedResponse <PostSummaryResponse>> getPosts(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size,
			@RequestParam(defaultValue = "createdAt") String sortBy) {
		return ResponseEntity.ok(postService.getPosts(page, size, sortBy));
	}
	
	@GetMapping("/{id}")
	public ResponseEntity <PostResponseDto> getById(@PathVariable UUID id) {
		return ResponseEntity.ok(postService.getById(id));
	}
	
	@GetMapping("/slug/{slug}")
	public ResponseEntity <PostResponseDto> getBySlug(@PathVariable String slug) {
		return ResponseEntity.ok(postService.getBySlug(slug));
	}
	
	@GetMapping("/author/{authorId}")
	public ResponseEntity <PagedResponse <PostSummaryResponse>> getByAuthor(
			@PathVariable UUID authorId,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size,
			@RequestParam(defaultValue = "createdAt") String sortBy) {
		
		return ResponseEntity.ok(postService.getByAuthorId(authorId, page, size, sortBy));
	}
	
	@PostMapping
	public ResponseEntity <PostResponseDto> create(
			@RequestBody PostCreateRequestDto postDate
	                                              ) {
		UUID authorId = UUID.fromString("444e5567-e89b-12d3-a456-426614174999");
		return ResponseEntity.ok(postService.create(postDate, authorId));
	}
	
	@PatchMapping("/{id}")
	public ResponseEntity <PostResponseDto> update(
			@PathVariable UUID id,
			@RequestBody PostUpdateRequestDto postDate
	                                              ) {
		UUID authorId = UUID.fromString("444e5567-e89b-12d3-a456-426614174999");
		return ResponseEntity.ok(postService.update(id, authorId, postDate));
	}
	
	@DeleteMapping("/{id}")
	public ResponseEntity <String> delete(
			@PathVariable UUID id) {
		UUID authorId = UUID.fromString("444e5567-e89b-12d3-a456-426614174999");
		postService.deletePost(authorId, id);
		return ResponseEntity.ok().body("Post with ID " + id + " was deleted");
	}
}
