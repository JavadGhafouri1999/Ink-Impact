package com.weblog.analyticservice.service;

import com.weblog.analyticservice.event.PostEvent;
import com.weblog.analyticservice.model.PostAnalytics;
import com.weblog.analyticservice.repository.PostAnalyticsRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

@Service
@RequiredArgsConstructor
public class PostAnalyticsService {
	private static final int    BATCH_SIZE = 100;
	private static final Logger log        = LoggerFactory.getLogger(PostAnalyticsService.class);
	
	private final PostAnalyticsRepository repository;
	
	private final List <PostAnalytics> buffer = new ArrayList <>();
	private final ReentrantLock        lock   = new ReentrantLock();
	
	
	private void flushInternal() {
		List <PostAnalytics> batch = new ArrayList <>(buffer);
		buffer.clear();
		log.info("Flushing {} PostAnalytics records to DB", batch.size());
		repository.batchSave(batch);
	}
	
	@Scheduled(fixedRate = 5000)
	public void scheduledFlush() {
		lock.lock();
		try {
			if(!buffer.isEmpty()) {
				flushInternal();
			}
		} finally {
			lock.unlock();
		}
	}
	
	private PostAnalytics convert(byte[] payload) {
		
		PostEvent event;
		
		try {
			event = PostEvent.parseFrom(payload);
		} catch(Exception e) {
			throw new RuntimeException("Failed to parse PostEvent", e);
		}
		
		return PostAnalytics
				.builder()
				.id(UUID.randomUUID()) // analytics id is always new
				.postId(event.getId()) // from producer
				.title(event.getTitle())
				.slug(event.getSlug())
				.authorId(event.getAuthorId())
				.tags(event.getTagsList()) // protobuf → List<String>
				.createdAt(Instant.ofEpochSecond(event.getCreatedAt().getSeconds()))
				.ingestedAt(LocalDateTime.now())
				.build();
	}
	
	public void buffer(byte[] payload) {
		PostAnalytics pa = convert(payload);
		
		lock.lock();
		try {
			buffer.add(pa);
			log.debug("Buffered POST event. Buffer size={}", buffer.size());
			if(buffer.size() >= BATCH_SIZE) {
				flushInternal();
			}
		} finally {
			lock.unlock();
		}
	}
}
