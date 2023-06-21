package com.forum.application.repository;

import com.forum.application.model.User;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
@Slf4j
@DataJpaTest
@RunWith(SpringRunner.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional(propagation = Propagation.NOT_SUPPORTED) // uncomment this to actually hit the database
class UserRepositoryTest {

    private final UserRepository userRepository;

    @Autowired
    public UserRepositoryTest(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    @Test
    void getIdByEmail() {
        String email = "user1@gmail.com";
        int userId = userRepository.fetchIdByEmail(email);
        log.debug("{} id is {}", email, userId);
    }


    @Test
    void fetchAllBlockedUserOf() {
        int userId = 1;
        userRepository.fetchAllBlockedUserOf(userId).forEach(user -> System.out.println(user.getName()));
    }
}