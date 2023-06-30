package com.forum.application.repository;

import com.forum.application.model.Mention;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MentionRepository extends JpaRepository<Mention, Integer> {
}