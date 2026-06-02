package com.weblog.postservice.repository;

import com.weblog.postservice.model.PostSearchDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface PostSearchRepository extends ElasticsearchRepository <PostSearchDocument, String> {

  List <PostSearchDocument> findByTitleContainingOrContentContaining(String title, String content);
}
