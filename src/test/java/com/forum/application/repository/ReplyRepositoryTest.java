package com.forum.application.repository;

import com.forum.application.model.Comment;
import com.forum.application.model.Reply;
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
class ReplyRepositoryTest {

    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final ReplyRepository replyRepository;

    @Autowired
    public ReplyRepositoryTest(UserRepository userRepository, CommentRepository commentRepository, ReplyRepository replyRepository) {
        this.userRepository = userRepository;
        this.commentRepository = commentRepository;
        this.replyRepository = replyRepository;
    }

    @Test
    void save() {
        User replier = userRepository.findById(1).orElseThrow();
        Comment comment = commentRepository.findById(2).orElseThrow();

        String body = String.format("%s replied in %s comment in %s post", replier.getName(), comment.getCommenter().getName(), comment.getPost().getAuthor().getName());
        Reply reply = Reply.builder()
                .body(body)
                .dateCreated(LocalDateTime.now())
                .comment(comment)
                .replier(replier)
                .build();

        replyRepository.save(reply);
        log.info("Replied Successfully");
    }

    @Test
    void delete() {
        int replyId = 1;
        replyRepository.deleteById(replyId);
        log.info("Reply Deleted Successfully");
    }
}