package com.weblog.commentservice.kafka;

import com.google.protobuf.Timestamp;
import com.weblog.commentservice.event.CommentEvent;
import com.weblog.commentservice.model.Comment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class KafkaProducer {

  private static final Logger log = LoggerFactory.getLogger(KafkaProducer.class);

  private final KafkaTemplate <String, byte[]> kafkaTemplate;

  public KafkaProducer(KafkaTemplate <String, byte[]> kafkaTemplate) {
	this.kafkaTemplate = kafkaTemplate;
  }

  public void sendEvent(Comment comment) {
	CommentEvent event = CommentEvent
			.newBuilder()
			.setId(comment.getId().toString())
			.setPostId(comment.getPostId().toString())
			.setContent(comment.getContent())
			.setUserId(comment.getUserId().toString())
			.setCreatedAt(nowTimestamp())
			.build();

	try {
	  kafkaTemplate.send("comment-event", event.toByteArray());
	} catch(Exception e) {
	  log.error("Error sending Comment-Creation event: {}", e.getMessage());
	}
  }

  private Timestamp nowTimestamp() {
	Instant now = Instant.now();
	return Timestamp
			.newBuilder()
			.setSeconds(now.getEpochSecond())
			.setNanos(now.getNano())
			.build();
  }
}
