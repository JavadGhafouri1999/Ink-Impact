CREATE TABLE post
(
    id               UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    author_id        UUID         NOT NULL,
    title            VARCHAR(255) NOT NULL UNIQUE,
    content          TEXT         NOT NULL,
    slug             VARCHAR(255),
    status           VARCHAR(50)  NOT NULL DEFAULT 'DRAFT',
    banner_image_url VARCHAR(500),
    view_count       BIGINT       NOT NULL DEFAULT 0,
    created_at       TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at       TIMESTAMP
);

CREATE TABLE post_tags
(
    post_id UUID NOT NULL REFERENCES post (id) ON DELETE CASCADE,
    tag     VARCHAR(255)
);

CREATE TABLE post_images
(
    post_id   UUID NOT NULL REFERENCES post (id) ON DELETE CASCADE,
    image_url VARCHAR(500)
);