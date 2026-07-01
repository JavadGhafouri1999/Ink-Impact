package com.weblog.postservice.config;

import com.weblog.postservice.dto.mapper.PostSearchDocumentMapper;
import com.weblog.postservice.model.Post;
import com.weblog.postservice.repository.PostRepository;
import com.weblog.postservice.repository.PostSearchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Runs on application startup to reindex all posts from PostgreSQL to Elasticsearch.
 * Ensures search index is in sync with database and includes viewCount for popularity sorting.
 */
@Component
@RequiredArgsConstructor
public class ElasticsearchReindexRunner {

  private final PostRepository           postRepo;
  private final PostSearchRepository     searchRepo;
  private final PostSearchDocumentMapper mapper;

  @EventListener(ApplicationReadyEvent.class)
  public void reindex() {

	List <Post> posts = postRepo.findAll();

	for(Post post : posts) {
	  searchRepo.save(mapper.toDocument(post));
	}
  }
}
