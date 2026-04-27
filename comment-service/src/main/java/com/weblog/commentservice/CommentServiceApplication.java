package com.weblog.commentservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class  CommentServiceApplication {
	public static void main(String[] args) {
		SpringApplication.run(CommentServiceApplication.class, args);
	}
}
