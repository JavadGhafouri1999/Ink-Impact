package com.weblog.analyticservice.repository;

import com.weblog.analyticservice.model.CommentAnalytics;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class CommentAnalyticsRepository {
	
	private final JdbcTemplate jdbc;
	
	private static final RowMapper <CommentAnalytics> MAPPER = (rs, rowNum) ->
			CommentAnalytics
					.builder()
					.id(UUID.fromString(rs.getString("id")))
					.commentId(rs.getString("comment_id"))
					.postId(rs.getString("post_id"))
					.userId(rs.getString("user_id"))
					.createdAt(rs.getTimestamp("created_at").toInstant())
					.ingestedAt(rs.getTimestamp("ingested_at").toLocalDateTime())
					.build();
	
	public void save(CommentAnalytics ca) {
		jdbc.update(con -> {
			var cs = con.prepareStatement("""
			                                  INSERT INTO comment_analytics
			                                  (id, comment_id, post_id, user_id, created_at, ingested_at)
			                                  VALUES (?, ?, ?, ?, ?, ?)
			                              """);
			cs.setObject(1, ca.getId());
			cs.setString(2, ca.getCommentId());
			cs.setString(3, ca.getPostId());
			cs.setString(4, ca.getUserId());
			cs.setTimestamp(5, Timestamp.from(ca.getCreatedAt()));
			cs.setTimestamp(6, Timestamp.valueOf(ca.getIngestedAt()));
			
			return cs;
		});
	}
	
	public void batchSave(List <CommentAnalytics> list) {
		if(list == null || list.isEmpty()) {
			return;
		}
		
		jdbc.batchUpdate("""
		                     INSERT INTO comment_analytics
		                     (id, comment_id, post_id, user_id, created_at, ingested_at)
		                     VALUES (?, ?, ?, ?, ?, ?)
		                 """,
		                 list, list.size(), (cs, ca) -> {
					Connection con = cs.getConnection();
					
					cs.setObject(1, ca.getId());
					cs.setString(2, ca.getCommentId());
					cs.setString(3, ca.getPostId());
					cs.setString(4, ca.getUserId());
					cs.setTimestamp(5, Timestamp.from(ca.getCreatedAt()));
					cs.setTimestamp(6, Timestamp.valueOf(ca.getIngestedAt()));
				});
	}
}
