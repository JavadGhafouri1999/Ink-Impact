/**
 * Purpose: Contract/blueprint. Defines WHAT methods exist, no implementation.
 *
 * <p>Used by: Controller, tests, anything that needs a PostService
 */
package com.weblog.postservice.service;

import com.weblog.postservice.dto.request.PostCreateRequestDto;
import com.weblog.postservice.dto.request.PostUpdateRequestDto;
import com.weblog.postservice.dto.response.PagedResponse;
import com.weblog.postservice.dto.response.PostResponseDto;
import com.weblog.postservice.dto.response.PostSummaryResponse;

import java.util.UUID;

public interface PostService {

  PostResponseDto create(PostCreateRequestDto request, UUID authorId, String role);

  PostResponseDto update(UUID id, UUID authorId, PostUpdateRequestDto request, String role);

  void deletePost(UUID authorId, UUID id, String role);

  PagedResponse <PostSummaryResponse> getPublishedPosts(int page, int size, String sortBy);

  PagedResponse <PostSummaryResponse> getAllPosts(String role, int page, int size, String sortBy);

  PostResponseDto getById(UUID id);

  boolean existsById(UUID id);

  PostResponseDto getBySlug(String slug);

  PagedResponse <PostSummaryResponse> getByAuthorId(
		  UUID authorId, int page, int size, String sortBy);

  PagedResponse <PostSummaryResponse> search(String q, int page, int size);

  PagedResponse <PostSummaryResponse> getByTag(String tag, int page, int size);
}
