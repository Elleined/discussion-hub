package com.forum.application.repository;

import com.forum.application.model.Mention;
import com.forum.application.model.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MentionRepository extends JpaRepository<Mention, Integer> {

    @Query("SELECT p.status FROM Post p WHERE p.id = :typeId")
    Status getPostStatus(@Param("typeId") int typeId);

    @Query("SELECT c.status FROM Comment c WHERE c.id = :typeId")
    Status getCommentStatus(@Param("typeId") int typeId);

    @Query("SELECT r.status FROM Reply r WHERE r.id = :typeId")
    Status getReplyStatus(@Param("typeId") int typeId);
}