package com.weblog.commentservice.service;

import com.weblog.commentservice.dto.request.CommentRequestDto;
import com.weblog.commentservice.dto.request.CommentUpdateDto;
import com.weblog.commentservice.dto.response.CommentResponseDto;
import com.weblog.commentservice.dto.response.PagedResponse;

import java.util.List;
import java.util.UUID;

public interface CommentService {
	
	PagedResponse <CommentResponseDto> findAllByPostId(UUID postId, int page, int size);
	
	void deleteByPostId(UUID postId);
	
	void deleteByUserId(UUID userId);
	
	CommentResponseDto findById(UUID id);
	
	List <CommentResponseDto> findAll();
	
	CommentResponseDto create(UUID userId, CommentRequestDto request);
	
	CommentResponseDto update(UUID commentId, CommentUpdateDto request);
}
