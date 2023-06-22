package com.forum.application.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserServiceTest {
    private final UserService userService;

    @Autowired
    public UserServiceTest(UserService userService) {
        this.userService = userService;
    }


    @Test
    void isBlockedBy() {
        int userId = 2;
        int userToCheckId = 1;
        assertTrue(userService.isBlockedBy(userId, userToCheckId), "User does not blocked the other user");
    }

    @Test
    void isYouBeenBlockedBy() {
        int yourId = 1;
        int suspectedBlockerId = 3;
        assertTrue(userService.isYouBeenBlockedBy(yourId, suspectedBlockerId), "This user does not blocked you");
    }
}