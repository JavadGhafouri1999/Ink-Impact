package com.weblog.commentservice.exception;

import java.util.UUID;

public class CommentNotFoundException extends RuntimeException {
	public CommentNotFoundException(UUID id) {
		super("Comment was not found - ID: " + id);
	}
}
