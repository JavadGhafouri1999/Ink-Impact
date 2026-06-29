package com.weblog.analyticservice.kafka;

import com.google.protobuf.InvalidProtocolBufferException;
import com.weblog.analyticservice.event.CommentEvent;
import com.weblog.analyticservice.event.PostEvent;
import com.weblog.analyticservice.service.CommentAnalyticsService;
import com.weblog.analyticservice.service.PostAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaConsumer {

  private static final Logger log = LoggerFactory.getLogger(KafkaConsumer.class);

  private final PostAnalyticsService    postAnalyticsService;
  private final CommentAnalyticsService commentAnalyticsService;

  @KafkaListener(topics = "post-event", groupId = "analytics-service")
  public void consumePost(byte[] event) throws InvalidProtocolBufferException {

	PostEvent postEvent = PostEvent.parseFrom(event);

	log.info(
			"Received Post Event: id={}, title={}, slug={}, tags={}, authorId={}, createdAt={}",
			postEvent.getId(),
			postEvent.getTitle(),
			postEvent.getSlug(),
			postEvent.getTagsList(),
			postEvent.getAuthorId(),
			postEvent.getCreatedAt()
			);
	postAnalyticsService.buffer(event);
  }

  @KafkaListener(topics = "comment-event", groupId = "analytics-service")
  public void consumeComment(byte[] event) throws InvalidProtocolBufferException {

	CommentEvent commentEvent = CommentEvent.parseFrom(event);

	log.info(
			"Received Comment Event: id={}, PostId={}, UserId={}, createdAt={}",
			commentEvent.getId(),
			commentEvent.getPostId(),
			commentEvent.getUserId(),
			commentEvent.getCreatedAt()
			);
	commentAnalyticsService.buffer(event);
  }


  @KafkaListener(topics = "event.DLT", groupId = "analytics-service")
  public void handleDlqPosts(byte[] event) {
	log.error("DLQ message received: {}", new String(event));
  }
}
