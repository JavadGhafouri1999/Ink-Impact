package com.weblog.postservice.event;

import java.util.UUID;

/**
 * Spring event record fired when a unique user views a post.
 * Published by: PostServiceImpl (in getById/getBySlug methods)
 * Consumed by: PostSyncListener.handleViewed()
 * This event is used to:
 * 1. Increment the viewCount on the Post entity
 * 2. Send a PostViewed event to Kafka for analytics service processing
 */
public record PostViewedEvent(
		UUID postId,    // ← The post being viewed
		UUID userId     // ← The user viewing it (for uniqueness tracking)
) { }
