package com.weblog.commentservice.service;

import com.weblog.commentservice.dto.mapper.CommentMapper;
import com.weblog.commentservice.dto.request.CommentRequestDto;
import com.weblog.commentservice.dto.request.CommentUpdateDto;
import com.weblog.commentservice.dto.response.CommentResponseDto;
import com.weblog.commentservice.dto.response.PagedResponse;
import com.weblog.commentservice.exception.CommentNotFoundException;
import com.weblog.commentservice.model.Comment;
import com.weblog.commentservice.repository.CommentRepository;
import com.weblog.postservice.grpc.PostExistsRequest;
import com.weblog.postservice.grpc.PostServiceGrpc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class CommentServiceImpl implements CommentService {
	
	private final CommentRepository                       commentRepo;
	private final PostServiceGrpc.PostServiceBlockingStub postStub;
	
	public CommentServiceImpl(CommentRepository commentRepo,
	                          PostServiceGrpc.PostServiceBlockingStub postStub) {
		this.commentRepo = commentRepo;
		this.postStub    = postStub;
	}
	
	@Override
	@Transactional(readOnly = true)
	public PagedResponse <CommentResponseDto> findAllByPostId(UUID postId, int page, int size) {
		Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
		
		Page <Comment> comments = commentRepo.findByPostId(postId, pageable);
		
		return PagedResponse.from(comments.map(CommentMapper::toDto));
	}
	
	@Override
	@Transactional
	public void deleteByPostId(UUID postId) {
		commentRepo.deleteByPostId(postId);
	}
	
	@Override
	@Transactional
	public void deleteByUserId(UUID userId) {
		commentRepo.deleteAllByUserId(userId);
	}
	
	@Override
	@Transactional(readOnly = true)
	public CommentResponseDto findById(UUID id) {
		Comment comment = commentRepo.findById(id)
		                             .orElseThrow(() -> new CommentNotFoundException(id));
		return CommentMapper.toDto(comment);
	}
	
	@Override
	@Transactional
	public CommentResponseDto create(UUID userId, CommentRequestDto request) {
		
		boolean postExists = postStub.postExists(
				PostExistsRequest.newBuilder()
				                 .setPostId(request.postId().toString())
				                 .build()).getExists();
		
		if(!postExists) {
			throw new RuntimeException("post does not exists - ID: " + request.postId());
		}
		
		Comment comment = commentRepo.save(CommentMapper.toModel(request, userId));
		return CommentMapper.toDto(comment);
	}
	
	@Override
	@Transactional
	public CommentResponseDto update(UUID commentId, CommentUpdateDto request) {
		Comment comment = commentRepo.findById(commentId)
		                             .orElseThrow(() -> new CommentNotFoundException(commentId));
		
		if(request.isPublished()) {
			comment.setPublished(true);
		}
		
		return CommentMapper.toDto(commentRepo.save(comment));
	}
	
	@Override
	@Transactional(readOnly = true)
	public List <CommentResponseDto> findAll() {
		List <Comment> comments = commentRepo.findAll();
		
		return comments.stream().map(CommentMapper::toDto).toList();
	}
}
