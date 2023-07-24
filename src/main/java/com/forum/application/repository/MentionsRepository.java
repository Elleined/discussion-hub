package com.forum.application.repository;

import com.forum.application.model.mention.Mentions;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MentionsRepository extends JpaRepository<Mentions, Integer> {
}