package com.forum.application.repository;

import com.forum.application.model.Mention;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface MentionRepository extends JpaRepository<Mention, Integer> {

    @Modifying
    @Transactional
    @Query(value = """
            UPDATE
            	tbl_mention_user
            SET
            	notification_status = "READ"
            WHERE
            	type_id
            IN (SELECT
            		c.comment_id
            	FROM
            		tbl_forum_post p,
            		tbl_forum_comment c
            	WHERE
            		p.post_id = c.post_id
            	AND
            		p.post_id = :postId
            )
            """, nativeQuery = true)
    void readAllComments(@Param("postId") int postId);

    @Modifying
    @Transactional
    @Query(value = """
            UPDATE
            	tbl_mention_user
            SET
            	notification_status = "READ"
            WHERE
            	type_id
            IN (SELECT
            		r.reply_id
            	FROM
            		tbl_forum_comment c,
            		tbl_forum_reply r
            	WHERE
            		c.comment_id = r.comment_id
            	AND
            		c.comment_id = :commentId
            )
            """, nativeQuery = true)
    void readAllReplies(@Param("commentId") int commentId);
}