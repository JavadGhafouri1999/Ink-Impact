package com.weblog.postservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

  @Bean(name = "postTaskExecutor")
  public Executor postTaskExecutor() {
	ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

	executor.setCorePoolSize(2);
	executor.setMaxPoolSize(5);
	executor.setQueueCapacity(100);

	executor.setThreadNamePrefix("post-async-");

	executor.initialize();

	return executor;
  }
}
