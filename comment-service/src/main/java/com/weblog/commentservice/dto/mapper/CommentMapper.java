package com.weblog.commentservice.dto.mapper;

import com.weblog.commentservice.dto.request.CommentRequestDto;
import com.weblog.commentservice.dto.response.CommentResponseDto;
import com.weblog.commentservice.model.Comment;

import java.util.UUID;

public final class CommentMapper {
	
	private CommentMapper() { }
	
	static public CommentResponseDto toDto(Comment comment) {
		return new CommentResponseDto(
				comment.getId(),
				comment.getPostId(),
				comment.getUserId(),
				comment.getContent(),
				comment.isPublished(),
				comment.getCreatedAt(),
				comment.getUpdatedAt()
		);
	}
	
	static public Comment toModel(CommentRequestDto request, UUID userId) {
		Comment comment = new Comment();
		comment.setPostId(request.postId());
		comment.setUserId(userId);
		comment.setContent(request.content());
		
		return comment;
	}
}
