package com.weblog.analyticservice.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public abstract class BatchIngestor<T> {

  private static final int BATCH_SIZE = 100;

  private final Logger    log   = LoggerFactory.getLogger(getClass());
  private final Queue <T> queue = new ConcurrentLinkedQueue <>();


  protected abstract T convert(byte[] payload);

  protected abstract void saveBatch(List <T> batch);

  protected abstract String name();


  public void flushInternal() {
	List <T> batch = new ArrayList <>(BATCH_SIZE);

	for(int i = 0; i < BATCH_SIZE; i++) {
	  T item = queue.poll();
	  if(item == null) { break; }
	  batch.add(item);
	}

	if(batch.isEmpty()) { return; }

	log.info("Flushing {} {} records", batch.size(), name());
	saveBatch(batch);
  }

  public void buffer(byte[] payload) {
	queue.add(convert(payload));
	int size = queue.size();

	if(size >= BATCH_SIZE) {
	  flushInternal();
	}
  }

  @Scheduled(fixedRate = 5000)
  public void flush() {
	flushInternal();
  }
}
