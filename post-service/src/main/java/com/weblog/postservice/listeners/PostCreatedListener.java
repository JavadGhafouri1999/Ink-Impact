package com.weblog.postservice.listeners;

import com.weblog.postservice.kafka.KafkaProducer;
import com.weblog.postservice.model.Post;
import com.weblog.postservice.model.PostSearchDocument;
import com.weblog.postservice.repository.PostSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class PostCreatedListener {

  private final PostSearchRepository repo;
  private final KafkaProducer        kafkaProducer;

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handle(PostCreatedEvent event) {
	Post post = event.post();

	// 1. Safe Elasticsearch Indexing
	try {
	  PostSearchDocument doc = new PostSearchDocument();
	  doc.setId(post.getId().toString());
	  doc.setTitle(post.getTitle());
	  doc.setContent(post.getContent());
	  doc.setTags(post.getTags());

	  repo.save(doc);
	  log.info("Successfully indexed post {} in Elasticsearch", post.getId());
	} catch(Exception e) {
	  log.error("Failed to index post {} in Elasticsearch. Out of sync!", post.getId(), e);
	  // Optional: send to a dead-letter queue or retry mechanism here
	}

	// 2. Safe Kafka Event Broadcasting
	try {
	  kafkaProducer.sendEvent(post);
	  log.info("Successfully dispatched post {} to Kafka", post.getId());
	} catch(Exception e) {
	  log.error("Failed to send post {} event to Kafka", post.getId(), e);
	}
  }
}
