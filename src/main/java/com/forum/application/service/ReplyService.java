package com.forum.application.service;

import com.forum.application.dto.ReplyDTO;
import com.forum.application.exception.ResourceNotFoundException;
import com.forum.application.model.Comment;
import com.forum.application.model.Reply;
import com.forum.application.model.Status;
import com.forum.application.model.User;
import com.forum.application.repository.CommentRepository;
import com.forum.application.repository.ReplyRepository;
import com.forum.application.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
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
                .status(Status.ACTIVE)
                .build();

        replyRepository.save(reply);
        log.debug("Reply with body of {} saved successfully!", reply.getBody());
        return reply.getId();
    }

    public void delete(int replyId) {
        this.setStatus(replyId);
        log.debug("Reply with id of {} are now inactive!", replyId);
    }

    public void updateReplyBody(int replyId, String newReplyBody) {
        Reply reply = replyRepository.findById(replyId).orElseThrow(() -> new ResourceNotFoundException("Reply with id of " + replyId + " does not exists!"));
        if (reply.getBody().equals(newReplyBody)) return;
        reply.setBody(newReplyBody);
        replyRepository.save(reply);
        log.debug("Reply with id of {} updated with the new body of {}", replyId, newReplyBody);
    }

    public List<ReplyDTO> getAllRepliesOf(int commentId) {
        Comment comment = commentRepository.findById(commentId).orElseThrow();
        return comment.getReplies()
                .stream()
                .filter(r -> r.getStatus() == Status.ACTIVE)
                .sorted(Comparator.comparing(Reply::getDateCreated))
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
                .replierName(reply.getReplier().getName())
                .dateCreated(reply.getDateCreated())
                .formattedDate(Formatter.formatDate(reply.getDateCreated()))
                .formattedTime(Formatter.formatTime(reply.getDateCreated()))
                .commentId(reply.getComment().getId())
                .replierId(reply.getReplier().getId())
                .replierPicture(reply.getReplier().getPicture())
                .status(reply.getStatus().name())
                .postId(reply.getComment().getPost().getId())
                .build();
    }

    public void setStatus(int replyId) {
        Reply reply = replyRepository.findById(replyId).orElseThrow(() -> new ResourceNotFoundException("Reply with id of " + replyId + " does not exists!"));
        reply.setStatus(Status.INACTIVE);
        replyRepository.save(reply);
    }
}
