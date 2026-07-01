package com.weblog.postservice.listeners;

import com.weblog.postservice.dto.mapper.PostSearchDocumentMapper;
import com.weblog.postservice.event.PostCreatedEvent;
import com.weblog.postservice.event.PostDeletedEvent;
import com.weblog.postservice.event.PostUpdatedEvent;
import com.weblog.postservice.event.PostViewedEvent;
import com.weblog.postservice.kafka.KafkaProducer;
import com.weblog.postservice.model.Post;
import com.weblog.postservice.repository.PostSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class PostSyncListener {

  private final PostSearchRepository     searchRepo;
  private final KafkaProducer            kafkaProducer;
  private final PostSearchDocumentMapper mapper;

  @Async("postTaskExecutor")
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleCreated(PostCreatedEvent event) {
	Post post = event.post();

	syncElastic(post);

	kafkaProducer.sendEvent(post);
  }

  @Async("postTaskExecutor")
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleUpdated(PostUpdatedEvent event) {
	Post post = event.post();

	syncElastic(post);
  }

  @Async("postTaskExecutor")
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleDeleted(PostDeletedEvent event) {
	try {
	  searchRepo.deleteById(event.postId().toString());

	  log.info(
			  "Deleted post {} from Elasticsearch",
			  event.postId()
			  );

	} catch(Exception e) {
	  log.error(
			  "Failed deleting post {} from Elasticsearch",
			  event.postId(),
			  e
			   );
	}
  }

  /**
   * Handles PostViewedEvent: when a unique user views a post.
   * Publishes the view event to Kafka for analytics service.
   * Analytics service will:
   * - Deduplicate views across distributed systems
   * - Track user behavior patterns
   * - Generate popularity metrics
   * ViewCount in the Post entity is incremented directly in PostServiceImpl.trackUniqueView()
   * to avoid waiting for this async operation.
   *
   * @param event PostViewedEvent with postId and userId
   */
  @Async("postTaskExecutor")
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleViewed(PostViewedEvent event) {
	try {
	  // ── Publish to Kafka for analytics service ──
	  // Analytics service will consume and process view events
	  kafkaProducer.sendViewEvent(event.postId(), event.userId());

	  log.debug("Published view event for post {} by user {} to Kafka", event.postId(), event.userId());

	} catch(Exception e) {
	  log.error("Failed publishing view event for post {} by user {}", event.postId(), event.userId(), e);
	}
  }

  /**
   * Syncs Post entity to Elasticsearch PostSearchDocument.
   * Keeps search index in sync with database changes.
   * Includes viewCount so Elasticsearch can support sorting by popularity.
   */
  private void syncElastic(Post post) {
	try {

	  searchRepo.save(mapper.toDocument(post));

	  log.info("Indexed post {} in Elasticsearch with viewCount {}", post.getId(), post.getViewCount());

	} catch(Exception e) {
	  log.error("Failed indexing post {}", post.getId(), e);
	}
  }
}
