-- User 1 comments on Post 1 (2 comments)
INSERT INTO comment (id, user_id, post_id, content, is_published, created_at, updated_at)
SELECT '323e4567-e89b-12d3-a456-426614174101',
       '223e4567-e89b-12d3-a456-426614174001',
       '123e4567-e89b-12d3-a456-426614174001',
       'Great post, very helpful!',
       TRUE,
       NOW(),
       NOW()
WHERE NOT EXISTS (SELECT 1 FROM comment WHERE id = '323e4567-e89b-12d3-a456-426614174101');

INSERT INTO comment (id, user_id, post_id, content, is_published, created_at, updated_at)
SELECT '323e4567-e89b-12d3-a456-426614174102',
       '223e4567-e89b-12d3-a456-426614174001',
       '123e4567-e89b-12d3-a456-426614174001',
       'I disagree with some points but good write-up.',
       TRUE,
       NOW(),
       NOW()
WHERE NOT EXISTS (SELECT 1 FROM comment WHERE id = '323e4567-e89b-12d3-a456-426614174102');

-- User 2 comments on Post 1 (2 comments)
INSERT INTO comment (id, user_id, post_id, content, is_published, created_at, updated_at)
SELECT '323e4567-e89b-12d3-a456-426614174103',
       '223e4567-e89b-12d3-a456-426614174002',
       '123e4567-e89b-12d3-a456-426614174001',
       'Could you explain more about this topic?',
       TRUE,
       NOW(),
       NOW()
WHERE NOT EXISTS (SELECT 1 FROM comment WHERE id = '323e4567-e89b-12d3-a456-426614174103');

INSERT INTO comment (id, user_id, post_id, content, is_published, created_at, updated_at)
SELECT '323e4567-e89b-12d3-a456-426614174104',
       '223e4567-e89b-12d3-a456-426614174002',
       '123e4567-e89b-12d3-a456-426614174001',
       'Looking forward to more posts like this.',
       TRUE,
       NOW(),
       NOW()
WHERE NOT EXISTS (SELECT 1 FROM comment WHERE id = '323e4567-e89b-12d3-a456-426614174104');

-- User 1 comments on Post 2 (2 comments)
INSERT INTO comment (id, user_id, post_id, content, is_published, created_at, updated_at)
SELECT '323e4567-e89b-12d3-a456-426614174105',
       '223e4567-e89b-12d3-a456-426614174001',
       '123e4567-e89b-12d3-a456-426614174002',
       'Nice perspective on this.',
       TRUE,
       NOW(),
       NOW()
WHERE NOT EXISTS (SELECT 1 FROM comment WHERE id = '323e4567-e89b-12d3-a456-426614174105');

INSERT INTO comment (id, user_id, post_id, content, is_published, created_at, updated_at)
SELECT '323e4567-e89b-12d3-a456-426614174106',
       '223e4567-e89b-12d3-a456-426614174001',
       '123e4567-e89b-12d3-a456-426614174002',
       'Thanks for sharing this!',
       FALSE,
       NOW(),
       NOW()
WHERE NOT EXISTS (SELECT 1 FROM comment WHERE id = '323e4567-e89b-12d3-a456-426614174106');

-- User 2 comments on Post 2 (2 comments)
INSERT INTO comment (id, user_id, post_id, content, is_published, created_at, updated_at)
SELECT '323e4567-e89b-12d3-a456-426614174107',
       '223e4567-e89b-12d3-a456-426614174002',
       '123e4567-e89b-12d3-a456-426614174002',
       'Well written, bookmarked.',
       TRUE,
       NOW(),
       NOW()
WHERE NOT EXISTS (SELECT 1 FROM comment WHERE id = '323e4567-e89b-12d3-a456-426614174107');

INSERT INTO comment (id, user_id, post_id, content, is_published, created_at, updated_at)
SELECT '323e4567-e89b-12d3-a456-426614174108',
       '223e4567-e89b-12d3-a456-426614174002',
       '123e4567-e89b-12d3-a456-426614174002',
       'Can you cite sources for this?',
       FALSE,
       NOW(),
       NOW()
WHERE NOT EXISTS (SELECT 1 FROM comment WHERE id = '323e4567-e89b-12d3-a456-426614174108');

-- User 1 comments on Post 3 (2 comments)
INSERT INTO comment (id, user_id, post_id, content, is_published, created_at, updated_at)
SELECT '323e4567-e89b-12d3-a456-426614174109',
       '223e4567-e89b-12d3-a456-426614174001',
       '123e4567-e89b-12d3-a456-426614174003',
       'This clarified a lot for me.',
       TRUE,
       NOW(),
       NOW()
WHERE NOT EXISTS (SELECT 1 FROM comment WHERE id = '323e4567-e89b-12d3-a456-426614174109');

INSERT INTO comment (id, user_id, post_id, content, is_published, created_at, updated_at)
SELECT '323e4567-e89b-12d3-a456-426614174110',
       '223e4567-e89b-12d3-a456-426614174001',
       '123e4567-e89b-12d3-a456-426614174003',
       'I had the same question, thanks!',
       TRUE,
       NOW(),
       NOW()
WHERE NOT EXISTS (SELECT 1 FROM comment WHERE id = '323e4567-e89b-12d3-a456-426614174110');

-- User 2 comments on Post 3 (2 comments)
INSERT INTO comment (id, user_id, post_id, content, is_published, created_at, updated_at)
SELECT '323e4567-e89b-12d3-a456-426614174111',
       '223e4567-e89b-12d3-a456-426614174002',
       '123e4567-e89b-12d3-a456-426614174003',
       'Not sure I agree, but good points.',
       TRUE,
       NOW(),
       NOW()
WHERE NOT EXISTS (SELECT 1 FROM comment WHERE id = '323e4567-e89b-12d3-a456-426614174111');

INSERT INTO comment (id, user_id, post_id, content, is_published, created_at, updated_at)
SELECT '323e4567-e89b-12d3-a456-426614174112',
       '223e4567-e89b-12d3-a456-426614174002',
       '123e4567-e89b-12d3-a456-426614174003',
       'Keep up the good work!',
       FALSE,
       NOW(),
       NOW()
WHERE NOT EXISTS (SELECT 1 FROM comment WHERE id = '323e4567-e89b-12d3-a456-426614174112');
