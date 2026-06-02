package com.weblog.commentservice.service;

import com.weblog.commentservice.dto.request.CommentRequestDto;
import com.weblog.commentservice.dto.request.CommentUpdateDto;
import com.weblog.commentservice.dto.response.CommentResponseDto;
import com.weblog.commentservice.dto.response.PagedResponse;

import java.util.List;
import java.util.UUID;

public interface CommentService {

  PagedResponse <CommentResponseDto> findAllByPostId(UUID postId, int page, int size);

  void deleteByPostId(UUID postId, String role);

  void deleteByUserId(UUID targetUserId, String role);

  CommentResponseDto findById(UUID id);

  List <CommentResponseDto> findAll();

  CommentResponseDto create(UUID user, String role, CommentRequestDto request);

  CommentResponseDto update(UUID commentId, String role, CommentUpdateDto request);
}
