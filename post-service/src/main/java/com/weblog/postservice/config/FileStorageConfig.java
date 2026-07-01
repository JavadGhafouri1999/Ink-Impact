package com.weblog.postservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class FileStorageConfig implements WebMvcConfigurer {

  @Value("${app.storage.post-images-dir}")
  private String uploadDir;

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
	registry
			.addResourceHandler("/uploads/posts/**")
			.addResourceLocations("file:" + Paths.get(uploadDir).toAbsolutePath() + "/");
  }
}
