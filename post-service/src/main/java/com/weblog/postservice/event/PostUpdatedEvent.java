package com.weblog.postservice.event;

import com.weblog.postservice.model.Post;

public record PostUpdatedEvent(Post post) { }
