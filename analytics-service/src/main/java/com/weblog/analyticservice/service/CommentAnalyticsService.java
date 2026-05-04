package com.weblog.analyticservice.service;

import com.weblog.analyticservice.event.CommentEvent;
import com.weblog.analyticservice.model.CommentAnalytics;
import com.weblog.analyticservice.repository.CommentAnalyticsRepository;
import com.weblog.analyticservice.utils.BatchIngestor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CommentAnalyticsService extends BatchIngestor <CommentAnalytics> {
	
	private final CommentAnalyticsRepository repository;
	
	
	@Override
	protected CommentAnalytics convert(byte[] payload) {
		try {
			CommentEvent e = CommentEvent.parseFrom(payload);
			return CommentAnalytics.builder()
			                       .id(UUID.randomUUID())
			                       .commentId(e.getId())
			                       .postId(e.getPostId())
			                       .userId(e.getUserId())
			                       .createdAt(Instant.ofEpochSecond(e.getCreatedAt().getSeconds()))
			                       .ingestedAt(LocalDateTime.now())
			                       .build();
		} catch(Exception ex) {
			throw new RuntimeException("Failed to parse CommentEvent", ex);
		}
	}
	
	@Override
	protected void saveBatch(List <CommentAnalytics> batch) {
	
	}
	
	@Override
	protected String name() {
		return "CommentAnalytics";
	}
	
}
