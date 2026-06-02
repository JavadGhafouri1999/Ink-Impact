package com.weblog.postservice.exception;

import java.util.UUID;

public class PostNotFoundException extends RuntimeException {

  public PostNotFoundException(UUID id) {
	super("Post was not found - ID: " + id);
  }

  public PostNotFoundException(String slug) {
	super("Post was not found - SLUG: " + slug);
  }
}
