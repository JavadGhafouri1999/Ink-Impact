package com.weblog.postservice.event;

import java.util.UUID;

public record PostDeletedEvent(UUID postId) { }
