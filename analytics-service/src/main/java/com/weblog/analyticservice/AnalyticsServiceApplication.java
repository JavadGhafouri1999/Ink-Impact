package com.weblog.analyticservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class AnalyticsServiceApplication {

  public static void main(String[] args) {

	SpringApplication.run(AnalyticsServiceApplication.class, args);
  }
}
