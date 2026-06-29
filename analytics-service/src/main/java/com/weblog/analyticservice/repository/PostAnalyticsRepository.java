package com.weblog.analyticservice.repository;

import com.weblog.analyticservice.dto.response.DailyPostCount;
import com.weblog.analyticservice.model.PostAnalytics;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class PostAnalyticsRepository {

  // RowMapper converts database rows to Java objects
  private static final RowMapper <PostAnalytics> MAPPER = (rs, rowNum) ->
		  PostAnalytics.builder()
					   .id(UUID.fromString(rs.getString("id")))
					   .postId(rs.getString("post_id"))
					   .title(rs.getString("title"))
					   .slug(rs.getString("slug"))
					   .authorId(rs.getString("author_id"))
					   .tags(List.of((String[]) rs.getArray("tags").getArray()))
					   .createdAt(rs.getTimestamp("created_at").toInstant())
					   .ingestedAt(rs.getTimestamp("ingested_at").toLocalDateTime())
					   .build();
  private final        JdbcTemplate              jdbc;

  public void save(PostAnalytics pa) {
	jdbc.update(con -> {
	  var ps = con.prepareStatement("""
									    INSERT INTO post_analytics
									    (id, post_id, title, slug, author_id, tags, created_at, ingested_at)
									    VALUES (?, ?, ?, ?, ?, ?, ?, ?)
									""");
	  ps.setObject(1, pa.getId());
	  ps.setString(2, pa.getPostId());
	  ps.setString(3, pa.getTitle());
	  ps.setString(4, pa.getSlug());
	  ps.setString(5, pa.getAuthorId());
	  ps.setArray(6, con.createArrayOf("text", pa.getTags().toArray()));
	  ps.setTimestamp(7, Timestamp.from(pa.getCreatedAt()));
	  ps.setTimestamp(8, Timestamp.valueOf(pa.getIngestedAt()));

	  return ps;
	});
  }

  public void batchSave(List <PostAnalytics> list) {
	if(list == null || list.isEmpty()) {
	  return;
	}

	jdbc.batchUpdate("""
					     INSERT INTO post_analytics
					     (id, post_id, title, slug, author_id, tags, created_at, ingested_at)
					     VALUES (?, ?, ?, ?, ?, ?, ?, ?)
					     ON CONFLICT (id) DO UPDATE SET
					         title = excluded.title,
					         slug = excluded.slug,
					         tags = excluded.tags
					 """,
					 list, list.size(), (ps, pa) -> {
			  Connection con = ps.getConnection();

			  ps.setObject(1, pa.getId());
			  ps.setString(2, pa.getPostId());
			  ps.setString(3, pa.getTitle());
			  ps.setString(4, pa.getSlug());
			  ps.setString(5, pa.getAuthorId());
			  ps.setArray(6, con.createArrayOf("text", pa.getTags().toArray()));
			  ps.setTimestamp(7, Timestamp.from(pa.getCreatedAt()));
			  ps.setTimestamp(8, Timestamp.valueOf(pa.getIngestedAt()));
			});
  }

  public int countTotalPost() {
	return jdbc.queryForObject(
			"SELECT COUNT(*) FROM post_analytics",
			Integer.class
							  );
  }

  public List <PostAnalytics> findLatest(int limit) {
	return jdbc.query(
			"SELECT * FROM post_analytics ORDER BY created_at DESC LIMIT ?",
			PostAnalyticsRepository.MAPPER,
			limit
					 );
  }

  public List <PostAnalytics> findByAuthor(String authorId) {
	return jdbc.query(
			"SELECT * FROM post_analytics WHERE author_id = ?",
			PostAnalyticsRepository.MAPPER,
			authorId);
  }

  public List <PostAnalytics> findBetween(Instant start, Instant end) {
	return jdbc.query(
			"SELECT * FROM post_analytics WHERE created_at BETWEEN ? AND ? ORDER BY created_at",
			PostAnalyticsRepository.MAPPER,
			start, end
					 );
  }

  public List <DailyPostCount> countPostsPerDay(int days) {
	return jdbc.query(
			"""
			SELECT date(created_at) AS day, COUNT(*) AS count
			FROM post_analytics
			GROUP BY day
			ORDER BY day DESC
			LIMIT ?
			""",
			(rs, n) -> new DailyPostCount(
					rs.getDate("day").toLocalDate(),
					rs.getLong("count")
			),
			days
					 );
  }
}
