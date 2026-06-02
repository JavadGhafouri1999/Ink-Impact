package com.weblog.postservice.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "posts")
public class PostSearchDocument {

  @Id
  private String id;

  @Field(type = FieldType.Text)
  private String title;

  @Field(type = FieldType.Text)
  private String slug;

  @Field(type = FieldType.Text)
  private String content;

  @Field(type = FieldType.Keyword)
  private String authorId;

  @Field(type = FieldType.Keyword)
  private List <String> tags;

  @Field(type = FieldType.Text)
  private String bannerImageUrl;

  @Field(type = FieldType.Keyword)
  private List <String> images;

  @Field(type = FieldType.Date)
  private LocalDateTime createdAt;
}
