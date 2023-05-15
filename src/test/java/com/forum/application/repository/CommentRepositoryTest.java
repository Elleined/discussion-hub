package com.forum.application.repository;

import com.forum.application.model.Comment;
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
class CommentRepositoryTest {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    @Autowired
    public CommentRepositoryTest(UserRepository userRepository, PostRepository postRepository, CommentRepository commentRepository) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
    }

    @Test
    void save() {
        User commenter = userRepository.findById(2).orElseThrow();
        Post post = postRepository.findById(3).orElseThrow();

        String body = String.format("%s commented in %s post", commenter.getName(), post.getAuthor().getName());
        Comment comment = Comment.builder()
                .body(body)
                .dateCreated(LocalDateTime.now())
                .post(post)
                .commenter(commenter)
                .build();

        commentRepository.save(comment);
        log.info("Commented Successfully");
    }

    @Test
    void delete() {
        int commentId = 1;
        commentRepository.deleteById(commentId);
        log.info("Comment Deleted Successfully");
    }
}