package com.forum.application.repository;

import com.forum.application.model.Post;
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

import java.time.LocalDateTime;

@Slf4j
@DataJpaTest
@RunWith(SpringRunner.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional(propagation = Propagation.NOT_SUPPORTED) // uncomment this to actually hit the database
class PostRepositoryTest {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Autowired
    public PostRepositoryTest(PostRepository postRepository, UserRepository userRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    @Test
    void save() {
        User author = userRepository.findById(1).orElseThrow();

        String body = String.format("%s created a post", author.getName());
        Post post = Post.builder()
                .body(body)
                .dateCreated(LocalDateTime.now())
                .author(author)
                .build();

        postRepository.save(post);
        log.info("Post Created Successfully");
    }

    @Test
    void delete() {
        int postId = 1;
        postRepository.deleteById(postId);
        log.info("Post Deleted Successfully");
    }

    @Test
    void getAll() {
        postRepository.findAll().forEach(System.out::println);
    }
}