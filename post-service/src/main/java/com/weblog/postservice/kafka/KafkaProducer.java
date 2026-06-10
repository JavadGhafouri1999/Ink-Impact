package com.weblog.postservice.kafka;

import com.google.protobuf.Timestamp;
import com.weblog.postservice.event.PostEvent;
import com.weblog.postservice.model.Post;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class KafkaProducer {

  private static final Logger log = LoggerFactory.getLogger(KafkaProducer.class);

  private final KafkaTemplate <String, byte[]>        KafkaTemplate;
  private final KafkaTemplate <String, PostViewEvent> viewEventKafkaTemplate; // ← For JSON view events

  public KafkaProducer(
		  KafkaTemplate <String, byte[]> kafkaTemplate,
		  KafkaTemplate <String, PostViewEvent> viewEventKafkaTemplate) {
	this.KafkaTemplate = kafkaTemplate;
	this.viewEventKafkaTemplate = viewEventKafkaTemplate;
  }

  /**
   * Sends post creation/update events as Protocol Buffer binary data.
   * Topic: "post-event"
   * Used by: PostSyncListener.handleCreated()
   */
  public void sendEvent(Post post) {
	PostEvent event = PostEvent.newBuilder()
							   .setId(post.getId().toString())
							   .setTitle(post.getTitle())
							   .setSlug(post.getSlug())
							   .setContent(post.getContent())
							   .addAllTags(post.getTags())
							   .setAuthorId(post.getAuthorId().toString())
							   .setCreatedAt(nowTimestamp())
							   .build();
	try {
	  KafkaTemplate.send("post-event", event.toByteArray());
	} catch(Exception e) {
	  log.error("Error sending PostCreation event: {}", e.getMessage());
	}
  }

  /**
   * Sends post view events as JSON data.
   * Topic: "post-view-event"
   * Used by: PostSyncListener.handleViewed()
   *
   * Analytics service consumes these events to:
   * - Track user engagement
   * - Deduplicate views across systems
   * - Calculate popularity metrics
   *
   * @param postId UUID of the post being viewed
   * @param userId UUID of the user viewing
   */
  public void sendViewEvent(UUID postId, UUID userId) {
	PostViewEvent viewEvent = new PostViewEvent(postId, userId, Instant.now());
	try {
	  viewEventKafkaTemplate.send("post-view-event", viewEvent);
	  log.debug("Sent view event to Kafka: post {} viewed by user {}", postId, userId);
	} catch(Exception e) {
	  log.error("Error sending PostView event for post {} by user {}: {}", postId, userId, e.getMessage());
	}
  }

  private Timestamp nowTimestamp() {
	Instant now = Instant.now();
	return Timestamp.newBuilder()
					.setSeconds(now.getEpochSecond())
					.setNanos(now.getNano())
					.build();
  }

  /**
   * DTO for post view events sent to Kafka as JSON.
   * Simple structure for analytics service to consume.
   */
  @Data
  @AllArgsConstructor
  public static class PostViewEvent {

	private UUID    postId;        // ← The post being viewed
	private UUID    userId;        // ← The user viewing
	private Instant viewedAt;      // ← Timestamp of the view
  }
}
