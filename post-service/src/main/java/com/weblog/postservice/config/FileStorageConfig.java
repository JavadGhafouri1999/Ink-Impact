package com.weblog.postservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class FileStorageConfig implements WebMvcConfigurer {

  @Value("${app.storage.post-images-dir:uploads/posts}")
  private String postImagesDir;

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
	String uploadPath = Paths.get(postImagesDir).toFile().getAbsolutePath();

	System.out.println("RESOURCE HANDLER PATH = " + uploadPath);

	registry.addResourceHandler("/uploads/posts/**").addResourceLocations("file:" + uploadPath + "/");
  }
}
