package com.weblog.apigateway.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserActivityProducer {

  private static final String                         TOPIC = "user-activity";
  private final        KafkaTemplate <String, Object> kafkaTemplate;

  public void sendActivityEvent(
		  String userId,
		  List <String> roles,
		  String path,
		  String method,
		  String ip,
		  String userAgent) {
	UserActivityEvent event = new UserActivityEvent(
			userId,
			roles,
			path,
			method,
			ip,
			userAgent,
			Instant.now().toString()
	);

	kafkaTemplate.send(TOPIC, userId, event);
	log.debug("Sent UserActivityEvent: {}", event);
  }
}
