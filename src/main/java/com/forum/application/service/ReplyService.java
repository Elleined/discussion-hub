package com.forum.application.service;

import com.forum.application.dto.ReplyDTO;
import com.forum.application.model.Comment;
import com.forum.application.model.Reply;
import com.forum.application.model.User;
import com.forum.application.repository.CommentRepository;
import com.forum.application.repository.ReplyRepository;
import com.forum.application.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReplyService {
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final ReplyRepository replyRepository;

    public ReplyService(UserRepository userRepository, CommentRepository commentRepository, ReplyRepository replyRepository) {
        this.userRepository = userRepository;
        this.commentRepository = commentRepository;
        this.replyRepository = replyRepository;
    }

    @Transactional
    public void save(int replierId, int commentId, String body) {
        User replier = userRepository.findById(replierId).orElseThrow();
        Comment comment = commentRepository.findById(commentId).orElseThrow();

        Reply reply = Reply.builder()
                .body(body)
                .dateCreated(LocalDateTime.now())
                .replier(replier)
                .comment(comment)
                .build();

        replyRepository.save(reply);
    }

    @Transactional
    public void delete(int replyId) {
        replyRepository.deleteById(replyId);
    }

    public List<ReplyDTO> getAllRepliesOf(int commentId) {
        Comment comment = commentRepository.findById(commentId).orElseThrow();
        return comment.getReplies()
                .stream()
                .map(this::convertToDTO)
                .toList();
    }

    private ReplyDTO convertToDTO(Reply reply) {
        return ReplyDTO.builder()
                .id(reply.getId())
                .body(reply.getBody())
                .dateCreated(reply.getDateCreated())
                .commentId(reply.getComment().getId())
                .replierId(reply.getReplier().getId())
                .build();
    }
}
