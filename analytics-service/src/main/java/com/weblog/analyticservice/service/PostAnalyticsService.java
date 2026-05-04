package com.weblog.analyticservice.service;

import com.weblog.analyticservice.event.PostEvent;
import com.weblog.analyticservice.model.PostAnalytics;
import com.weblog.analyticservice.repository.PostAnalyticsRepository;
import com.weblog.analyticservice.utils.BatchIngestor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PostAnalyticsService extends BatchIngestor <PostAnalytics> {
	
	private final PostAnalyticsRepository repository;
	
	
	@Override
	protected PostAnalytics convert(byte[] payload) {
		try {
			PostEvent e = PostEvent.parseFrom(payload);
			return PostAnalytics.builder()
			                    .id(UUID.randomUUID())
			                    .postId(e.getId())
			                    .title(e.getTitle())
			                    .slug(e.getSlug())
			                    .authorId(e.getAuthorId())
			                    .tags(e.getTagsList())
			                    .createdAt(Instant.ofEpochSecond(e.getCreatedAt().getSeconds()))
			                    .ingestedAt(LocalDateTime.now())
			                    .build();
		} catch(Exception ex) {
			throw new RuntimeException("Failed to parse PostEvent", ex);
		}
	}
	
	@Override
	protected void saveBatch(List <PostAnalytics> batch) {
		repository.batchSave(batch);
	}
	
	@Override
	protected String name() {
		return "PostAnalytics";
	}
}
