package com.weblog.postservice.event;

import com.weblog.postservice.model.Post;

public record PostCreatedEvent(
		Post post
) { }
