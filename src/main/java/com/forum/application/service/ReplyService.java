package com.forum.application.service;

import com.forum.application.dto.ReplyDTO;
import com.forum.application.exception.ResourceNotFoundException;
import com.forum.application.model.Comment;
import com.forum.application.model.Reply;
import com.forum.application.model.User;
import com.forum.application.repository.CommentRepository;
import com.forum.application.repository.ReplyRepository;
import com.forum.application.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class ReplyService {
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final ReplyRepository replyRepository;

    public int save(int replierId, int commentId, String body) {
        User replier = userRepository.findById(replierId).orElseThrow(() -> new ResourceNotFoundException("User with id of " + replierId + " does not exists!"));
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new ResourceNotFoundException("Comment with id of " + commentId + " does not exists!"));

        Reply reply = Reply.builder()
                .body(body)
                .dateCreated(LocalDateTime.now())
                .replier(replier)
                .comment(comment)
                .build();

        replyRepository.save(reply);
        log.debug("Reply with body of {} saved successfully!", reply.getBody());
        return reply.getId();
    }

    public void delete(int replyId) {
        replyRepository.deleteById(replyId);
        log.debug("Reply with id of {} deleted successfully!", replyId);
    }

    public List<ReplyDTO> getAllRepliesOf(int commentId) {
        Comment comment = commentRepository.findById(commentId).orElseThrow();
        return comment.getReplies()
                .stream()
                .map(this::convertToDTO)
                .toList();
    }

    public ReplyDTO getById(int replyId) {
        Reply reply = replyRepository.findById(replyId).orElseThrow(() -> new ResourceNotFoundException("Reply with id of " + replyId + " does not exists!"));
        return this.convertToDTO(reply);
    }

    private ReplyDTO convertToDTO(Reply reply) {
        return ReplyDTO.builder()
                .id(reply.getId())
                .body(reply.getBody())
                .dateCreated(reply.getDateCreated())
                .commentId(reply.getComment().getId())
                .replierId(reply.getReplier().getId())
                .replierPicture(reply.getReplier().getPicture())
                .build();
    }
}
