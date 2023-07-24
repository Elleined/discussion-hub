package com.forum.application.service;

import com.forum.application.model.ModalTracker;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Slf4j
class UserServiceTest {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceTest(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @Test
    void encodePassword() {
        String password = "Denielle";
        String encodedPassword = passwordEncoder.encode(password);
        log.debug("Encoded password {}", encodedPassword);
    }

    @Test
    void isPasswordCorrect() {
        String inputtedPassword = "Denielle";
        assertTrue(passwordEncoder.matches(inputtedPassword, "$2a$10$E8GLhQsBAP9cDQ4ou0uGQOLWv3DArHgKX6hsky4qF7VEW1ULN53Am"), "Password correct");
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

    @Test
    void isModalOpen() {
        int userId = 1;
        int associatedTypeId = 5; // Post or Comment id
        ModalTracker.Type type = ModalTracker.Type.COMMENT;
        assertTrue(userService.isModalOpen(userId, associatedTypeId, type), "This user does not have an open modal!");
    }

}