package com.weblog.postservice.kafka;

import com.google.protobuf.Timestamp;
import com.weblog.postservice.event.PostEvent;
import com.weblog.postservice.model.Post;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class KafkaProducer {
	
	private static final Logger log = LoggerFactory.getLogger(KafkaProducer.class);
	
	private final KafkaTemplate <String, byte[]> KafkaTemplate;
	
	public KafkaProducer(KafkaTemplate <String, byte[]> kafkaTemplate) {
		this.KafkaTemplate = kafkaTemplate;
	}
	
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
	
	private Timestamp nowTimestamp() {
		Instant now = Instant.now();
		return Timestamp.newBuilder()
		                .setSeconds(now.getEpochSecond())
		                .setNanos(now.getNano())
		                .build();
	}
}
