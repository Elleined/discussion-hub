package com.forum.application.repository;

import com.forum.application.model.Mention;
import com.forum.application.model.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MentionRepository extends JpaRepository<Mention, Integer> {

    @Query("SELECT p.status FROM Post p WHERE p.id = :typeId")
    Status isPostDeleted(@Param("typeId") int typeId);
}