package com.weblog.authservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class FileStorageConfig implements WebMvcConfigurer {

  @Value("${app.storage.auth-images-dir}")
  private String uploadDir;

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {

	registry
			.addResourceHandler("/uploads/auth/**")
			.addResourceLocations(
					"file:" + Paths.get(uploadDir).toAbsolutePath() + "/"
								 );
  }
}
