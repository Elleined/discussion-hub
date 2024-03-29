package com.forum.application.service;

import com.forum.application.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class MentionServiceTest {

    @Autowired
    private MentionService mentionService;

    @Autowired
    private UserService userService;

    @Test
    void getReceiveCommentMentions() {
        User currentUser = userService.getById(1);
        mentionService.getUnreadCommentMentions(currentUser).forEach(System.out::println);
    }

    @Test
    void getReceiveReplyMentions() {
        User currentUser = userService.getById(1);
        mentionService.getUnreadReplyMentions(currentUser).forEach(System.out::println);
    }
}