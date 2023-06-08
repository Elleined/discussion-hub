package com.forum.application.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class CommentServiceTest {

    private final CommentService commentService;

    @Autowired
    public CommentServiceTest(CommentService commentService) {
        this.commentService = commentService;
    }

    @Test
    void isValidUpvoteValue() {
        int newUpvoteValue = 3;
        int respondentId = 1;
        commentService.updateUpvote(respondentId, 91, newUpvoteValue);
    }
}