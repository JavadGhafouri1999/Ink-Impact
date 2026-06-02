package com.weblog.postservice.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;

@Configuration
@EnableCaching
public class RedisConfig {

  @Bean
  public RedisCacheConfiguration cacheConfiguration() {
	ObjectMapper redisMapper = JsonMapper.builder()
										 .addModule(new JavaTimeModule())
										 .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
										 .activateDefaultTyping(
												 BasicPolymorphicTypeValidator.builder()
																			  .allowIfBaseType(Object.class)
																			  .build(),
												 ObjectMapper.DefaultTyping.NON_FINAL,
												 JsonTypeInfo.As.PROPERTY
															   )
										 .build();

	return RedisCacheConfiguration
			.defaultCacheConfig()
			.entryTtl(Duration.ofMinutes(10))
			.disableCachingNullValues()
			.serializeValuesWith(
					RedisSerializationContext.SerializationPair.fromSerializer(
							new GenericJackson2JsonRedisSerializer(redisMapper)
																			  ));
  }

  @Bean
  public RedisCacheManager cacheManager(
		  RedisConnectionFactory connectionFactory,
		  RedisCacheConfiguration cacheConfiguration) {
	return RedisCacheManager.builder(connectionFactory)
							.cacheDefaults(cacheConfiguration)
							.build();
  }
}
