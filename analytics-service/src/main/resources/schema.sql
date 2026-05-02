CREATE TABLE post_analytics
(
	id          uuid PRIMARY KEY,
	post_id     VARCHAR(255)             NOT NULL,
	title       VARCHAR(500)             NOT NULL,
	slug        VARCHAR(500)             NOT NULL,
	author_id   VARCHAR(255)             NOT NULL,
	tags        TEXT[], -- PostgreSQL array type for tags
	created_at  TIMESTAMP WITH TIME ZONE NOT NULL,
	ingested_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE comment_analytics
(
	id          uuid PRIMARY KEY,
	comment_id  VARCHAR(255)             NOT NULL,
	post_id     VARCHAR(255)             NOT NULL,
	user_id     VARCHAR(255)             NOT NULL,
	created_at  TIMESTAMP WITH TIME ZONE NOT NULL,
	ingested_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- Create indexes for common query patterns
CREATE INDEX idx_post_analytics_created_at ON post_analytics (created_at);
CREATE INDEX idx_post_analytics_author_id ON post_analytics (author_id);
CREATE INDEX idx_post_analytics_tags ON post_analytics USING gin (tags);
CREATE INDEX idx_comment_analytics_post_id ON comment_analytics (post_id);
CREATE INDEX idx_comment_analytics_created_at ON comment_analytics (created_at);
