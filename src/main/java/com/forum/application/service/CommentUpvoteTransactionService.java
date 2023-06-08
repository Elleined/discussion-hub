package com.forum.application.service;

import com.forum.application.exception.ResourceNotFoundException;
import com.forum.application.model.Comment;
import com.forum.application.model.CommentUpvoteTransaction;
import com.forum.application.model.User;
import com.forum.application.repository.CommentRepository;
import com.forum.application.repository.CommentUpvoteTransactionRepository;
import com.forum.application.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j

public class CommentUpvoteTransactionService {

    private final CommentUpvoteTransactionRepository commentUpvoteTransactionRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    public int save(int respondentId, int commentId) {
        User respondent = userRepository.findById(respondentId).orElseThrow(() -> new ResourceNotFoundException("User with id of " + respondentId + " does not exists!"));
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new ResourceNotFoundException("Comment with id of " + commentId + " does not exists!"));

        CommentUpvoteTransaction commentUpvoteTransaction = CommentUpvoteTransaction.builder()
                .comment(comment)
                .respondent(respondent)
                .build();

        commentUpvoteTransactionRepository.save(commentUpvoteTransaction);
        log.debug("CommentUpvoteTransaction saved successfully!");
        return commentUpvoteTransaction.getId();
    }

    public boolean isUserAlreadyUpvoteComment(int respondentId, int commentId) {
        User respondent = userRepository.findById(respondentId).orElseThrow(() -> new ResourceNotFoundException("User with id of " + respondentId + " does not exists!"));
        List<CommentUpvoteTransaction> commentUpvoteTransactionServices = respondent.getCommentUpvoteTransactions();

        return commentUpvoteTransactionServices.stream()
                .map(CommentUpvoteTransaction::getComment)
                .anyMatch(comment -> comment.getId() == commentId);
    }
}
