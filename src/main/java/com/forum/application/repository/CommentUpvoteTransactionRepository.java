package com.forum.application.repository;

import com.forum.application.model.CommentUpvoteTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentUpvoteTransactionRepository extends JpaRepository<CommentUpvoteTransaction, Integer> {
}