-- Only insert if posts table is empty
INSERT INTO post (id, title, content, slug, published, created_at, updated_at, author_id)
SELECT '123e4567-e89b-12d3-a456-426614174000',
       'Getting Started with Spring Boot',
       'Spring Boot makes it easy to create stand-alone, production-grade Spring based Applications that you can "just run".',
       'getting-started-spring-boot',
       true,
       now(),
       now(),
       'a1b2c3d4-e89b-12d3-a456-426614174000'
	WHERE NOT EXISTS (SELECT 1 FROM post WHERE id = '123e4567-e89b-12d3-a456-426614174000');

INSERT INTO post (id, title, content, slug, published, created_at, updated_at, author_id)
SELECT '123e4567-e89b-12d3-a456-426614174001',
       'Understanding Docker Compose',
       'Docker Compose is a tool for defining and running multi-container Docker applications.',
       'understanding-docker-compose',
       true,
       now(),
       now(),
       'a1b2c3d4-e89b-12d3-a456-426614174000'
	WHERE NOT EXISTS (SELECT 1 FROM post WHERE id = '123e4567-e89b-12d3-a456-426614174001');

INSERT INTO post (id, title, content, slug, published, created_at, updated_at, author_id)
SELECT '123e4567-e89b-12d3-a456-426614174002',
       'Draft Post - Microservices',
       'This is a draft about microservices architecture.',
       'draft-microservices',
       false,
       now(),
       now(),
       'b2c3d4e5-e89b-12d3-a456-426614174000'
	WHERE NOT EXISTS (SELECT 1 FROM post WHERE id = '123e4567-e89b-12d3-a456-426614174002');
