package com.weblog.commentservice.controller;

import com.weblog.commentservice.dto.request.CommentRequestDto;
import com.weblog.commentservice.dto.request.CommentUpdateDto;
import com.weblog.commentservice.dto.response.CommentResponseDto;
import com.weblog.commentservice.dto.response.PagedResponse;
import com.weblog.commentservice.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

//UUID userId, CommentRequestDto request

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {
	
	private final CommentService commentService;
	
	@PostMapping
	public ResponseEntity <CommentResponseDto> create(
			@Valid @RequestBody CommentRequestDto request) {
		UUID userId = UUID.fromString("444e5567-e89b-12d3-a456-426614174999");
		return ResponseEntity.ok(commentService.create(userId, request));
	}
	
	@PatchMapping("/{id}")
	public ResponseEntity <CommentResponseDto> update(
			@PathVariable UUID id,
			@Valid @RequestBody CommentUpdateDto request) {
		return ResponseEntity.ok(commentService.update(id, request));
	}
	
	@GetMapping("/{id}")
	public ResponseEntity <CommentResponseDto> getById(
			@PathVariable UUID id) {
		return ResponseEntity.ok(commentService.findById(id));
	}
	
	@GetMapping
	public ResponseEntity <List <CommentResponseDto>> getAll() {
		return ResponseEntity.ok(commentService.findAll());
	}
	
	@GetMapping("/postcomment/{postId}")
	public ResponseEntity <PagedResponse <CommentResponseDto>> getByPostId(
			@PathVariable UUID postId,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		
		return ResponseEntity.ok(commentService.findAllByPostId(postId, page, size));
	}
	
	@DeleteMapping("/postcomment/{postId}")
	public ResponseEntity <String> deleteByPostId(
			@PathVariable UUID postId) {
		commentService.deleteByPostId(postId);
		return ResponseEntity.ok("Comments for post Deleted - Post-ID: " + postId);
	}
	
	@DeleteMapping("/usercomment/{userId}")
	public ResponseEntity <String> deleteByUserId(
			@PathVariable UUID userId) {
		commentService.deleteByUserId(userId);
		return ResponseEntity.ok("Comments for user Deleted - User-ID: " + userId);
	}
}
