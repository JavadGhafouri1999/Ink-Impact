package com.weblog.postservice.listeners;

import com.weblog.postservice.model.Post;

public record PostCreatedEvent(
		Post post
) { }
