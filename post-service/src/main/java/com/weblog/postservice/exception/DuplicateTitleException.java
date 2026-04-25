package com.weblog.postservice.exception;

public class DuplicateTitleException extends RuntimeException {
	public DuplicateTitleException(String title) {
		super("Post with title '" + title + "' already exists");
	}
}
