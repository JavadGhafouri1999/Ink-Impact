package com.weblog.postservice.listeners;

import com.weblog.postservice.event.PostCreatedEvent;
import com.weblog.postservice.event.PostDeletedEvent;
import com.weblog.postservice.event.PostUpdatedEvent;
import com.weblog.postservice.kafka.KafkaProducer;
import com.weblog.postservice.model.Post;
import com.weblog.postservice.model.PostSearchDocument;
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

  private final PostSearchRepository searchRepo;
  private final KafkaProducer        kafkaProducer;

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

  private void syncElastic(Post post) {
	try {
	  PostSearchDocument doc = new PostSearchDocument();

	  doc.setId(post.getId().toString());
	  doc.setTitle(post.getTitle());
	  doc.setSlug(post.getSlug());

	  doc.setContent(post.getContent());
	  doc.setTags(post.getTags());

	  doc.setAuthorId(post.getAuthorId().toString());
	  doc.setCreatedAt(post.getCreatedAt().toString());
	  doc.setStatus(post.getStatus().name());

	  doc.setBannerImageUrl(post.getBannerImageUrl());
	  doc.setImages(post.getImages());

	  searchRepo.save(doc);

	  log.info("Indexed post {} in Elasticsearch", post.getId());

	} catch(Exception e) {
	  log.error("Failed indexing post {}", post.getId(), e);
	}
  }
}
