package com.weblog.postservice.config;

import com.weblog.postservice.model.Post;
import com.weblog.postservice.model.PostSearchDocument;
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

  private final PostRepository       postRepo;
  private final PostSearchRepository searchRepo;

  @EventListener(ApplicationReadyEvent.class)
  public void reindex() {

	List <Post> posts = postRepo.findAll();

	for(Post post : posts) {
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

	  // ── Include viewCount in Elasticsearch index ──
	  // Allows sorting and filtering by popularity in search results
	  doc.setViewCount(post.getViewCount());

	  searchRepo.save(doc);
	}
  }
}
