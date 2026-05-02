package com.weblog.analyticservice.service;

import com.weblog.analyticservice.event.CommentEvent;
import com.weblog.analyticservice.model.CommentAnalytics;
import com.weblog.analyticservice.repository.CommentAnalyticsRepository;
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
public class CommentAnalyticsService {
	
	private static final int    BATCH_SIZE = 100;
	private static final Logger log        = LoggerFactory.getLogger(CommentAnalyticsService.class);
	
	private final CommentAnalyticsRepository repository;
	
	private final List <CommentAnalytics> buffer = new ArrayList <>();
	
	private final ReentrantLock lock = new ReentrantLock();
	
	private void flushInternal() {
		List <CommentAnalytics> batch = new ArrayList <>(buffer);
		buffer.clear();
		log.info("Flushing {} CommentAnalytics records to DB", batch.size());
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
	
	private CommentAnalytics convert(byte[] payload) {
		CommentEvent event;
		try {
			event = CommentEvent.parseFrom(payload);
		} catch(Exception e) {
			throw new RuntimeException("Failed to parse CommentEvent", e);
		}
		
		return CommentAnalytics
				.builder()
				.id(UUID.randomUUID())
				.commentId(event.getId())
				.postId(event.getPostId())
				.userId(event.getUserId())
				.createdAt(Instant.ofEpochSecond(event.getCreatedAt().getSeconds()))
				.ingestedAt(LocalDateTime.now())
				.build();
	}
	
	public void buffer(byte[] payload) {
		CommentAnalytics ca = convert(payload);
		
		lock.lock();
		try {
			buffer.add(ca);
			log.debug("Buffered COMMENT event. Buffer size={}", buffer.size());
			if(buffer.size() >= BATCH_SIZE) {
				flushInternal();
			}
		} finally {
			lock.unlock();
		}
	}
}
