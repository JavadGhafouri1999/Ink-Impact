package com.weblog.postservice.dto.mapper;

import com.weblog.postservice.model.Post;
import com.weblog.postservice.model.PostSearchDocument;
import org.springframework.stereotype.Component;

@Component
public class PostSearchDocumentMapper {

  public PostSearchDocument toDocument(Post post) {
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
	doc.setViewCount(post.getViewCount());
	return doc;
  }
}
