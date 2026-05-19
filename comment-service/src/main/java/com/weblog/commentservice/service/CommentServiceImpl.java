package com.weblog.commentservice.service;

import com.weblog.commentservice.dto.mapper.CommentMapper;
import com.weblog.commentservice.dto.request.CommentRequestDto;
import com.weblog.commentservice.dto.request.CommentUpdateDto;
import com.weblog.commentservice.dto.response.CommentResponseDto;
import com.weblog.commentservice.dto.response.PagedResponse;
import com.weblog.commentservice.exception.CommentNotFoundException;
import com.weblog.commentservice.exception.PostNotFoundException;
import com.weblog.commentservice.exception.UnauthorizedException;
import com.weblog.commentservice.kafka.KafkaProducer;
import com.weblog.commentservice.model.Comment;
import com.weblog.commentservice.post.grpc.PostExistsRequest;
import com.weblog.commentservice.post.grpc.PostServiceGrpc;
import com.weblog.commentservice.repository.CommentRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

  private final CommentRepository commentRepo;
  private final PostServiceGrpc.PostServiceBlockingStub postStub;
  private final KafkaProducer kafkaProducer;

  private Comment findAndVerifyOwnership(UUID commentId, UUID userId) {
    Comment c =
        commentRepo.findById(commentId).orElseThrow(() -> new CommentNotFoundException(commentId));

    if (!c.getUserId().equals(userId)) {
      throw new UnauthorizedException("You can only modify your own comments");
    }

    return c;
  }

  @Override
  @Transactional(readOnly = true)
  public PagedResponse<CommentResponseDto> findAllByPostId(UUID postId, int page, int size) {
    Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

    Page<Comment> comments = commentRepo.findByPostId(postId, pageable);

    return PagedResponse.from(comments.map(CommentMapper::toDto));
  }

  @Override
  @Transactional
  public void deleteByPostId(UUID postId, String role) {

    if (!role.equals("ADMIN") && !role.equals("OWNER")) {
      throw new UnauthorizedException("You are not allowed to delete all comments for a post");
    }

    commentRepo.deleteByPostId(postId);
  }

  @Override
  @Transactional
  public void deleteByUserId(UUID targetUserId, String role) {

    if (!role.equals("ADMIN") && !role.equals("OWNER")) {
      throw new UnauthorizedException("You are not allowed to delete all comments for a post");
    }
    commentRepo.deleteAllByUserId(targetUserId);
  }

  @Override
  @Transactional(readOnly = true)
  public CommentResponseDto findById(UUID id) {
    Comment comment = commentRepo.findById(id).orElseThrow(() -> new CommentNotFoundException(id));
    return CommentMapper.toDto(comment);
  }

  @Override
  @Transactional
  public CommentResponseDto create(UUID user, String role, CommentRequestDto request) {

    if (!role.equals("USER")
        && !role.equals("AUTHOR")
        && !role.equals("ADMIN")
        && !role.equals("OWNER")) {
      throw new UnauthorizedException("You are not allowed to write comments");
    }

    boolean postExists =
        postStub
            .postExists(
                PostExistsRequest.newBuilder().setPostId(request.postId().toString()).build())
            .getExists();

    if (!postExists) {
      throw new PostNotFoundException("post does not exists - ID: " + request.postId());
    }

    Comment comment = commentRepo.save(CommentMapper.toModel(request, user));

    // Kafka-Event
    kafkaProducer.sendEvent(comment);

    return CommentMapper.toDto(comment);
  }

  @Override
  @Transactional
  public CommentResponseDto update(UUID commentId, String role, CommentUpdateDto request) {

    if (!role.equals("ADMIN") && !role.equals("OWNER")) {
      throw new UnauthorizedException("You are not allowed to update comments");
    }

    Comment comment =
        commentRepo.findById(commentId).orElseThrow(() -> new CommentNotFoundException(commentId));

    if (request.isPublished()) {
      comment.setPublished(true);
    }

    return CommentMapper.toDto(commentRepo.save(comment));
  }

  @Override
  @Transactional(readOnly = true)
  public List<CommentResponseDto> findAll() {
    List<Comment> comments = commentRepo.findAll();

    return comments.stream().map(CommentMapper::toDto).toList();
  }
}
