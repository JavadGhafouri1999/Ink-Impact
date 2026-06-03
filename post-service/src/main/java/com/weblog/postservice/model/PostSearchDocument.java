package com.weblog.postservice.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "posts")
@Setting(settingPath = "/elasticsearch/settings.json")
public class PostSearchDocument {

  @Id
  private String id;

  @Field(
		  type = FieldType.Text,
		  analyzer = "autocomplete_analyzer",
		  searchAnalyzer = "standard"
  )
  private String title;

  @Field(type = FieldType.Keyword)
  private String slug;

  @Field(
		  type = FieldType.Text,
		  analyzer = "autocomplete_analyzer",
		  searchAnalyzer = "standard"
  )
  private String content;

  @Field(type = FieldType.Keyword)
  private List <String> tags;

  @Field(type = FieldType.Keyword)
  private String authorId;

  @Field(type = FieldType.Text)
  private String bannerImageUrl;

  @Field(type = FieldType.Keyword)
  private List <String> images;

  @Field(type = FieldType.Keyword)
  private String createdAt;

  @Field(type = FieldType.Keyword)
  private String status;  // DRAFT, PUBLISHED

  @Field(type = FieldType.Long)
  private Long viewCount;  // For aggregations later
}
