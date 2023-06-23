package com.forum.application.service;

import com.forum.application.dto.ReplyDTO;
import com.forum.application.exception.ResourceNotFoundException;
import com.forum.application.model.*;
import com.forum.application.repository.CommentRepository;
import com.forum.application.repository.ReplyRepository;
import jakarta.servlet.http.HttpSession;
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
    private final UserService userService;
    private final CommentRepository commentRepository;
    private final ReplyRepository replyRepository;
    private final HttpSession session;

    public int save(int replierId, int commentId, String body) {
        User replier = userService.getById(replierId);
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new ResourceNotFoundException("Comment with id of " + commentId + " does not exists!"));

        Reply reply = Reply.builder()
                .body(body)
                .dateCreated(LocalDateTime.now())
                .replier(replier)
                .comment(comment)
                .status(Status.ACTIVE)
                .notificationStatus(NotificationStatus.UNREAD)
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

    public void updateNotificationStatus(int replyId, NotificationStatus newStatus) {
        Reply reply = replyRepository.findById(replyId).orElseThrow(() -> new ResourceNotFoundException("Reply with id of " + replyId + " does not exists!"));
        reply.setNotificationStatus(newStatus);
        replyRepository.save(reply);
        log.debug("Reply with id of {} notification status updated successfully to {}", reply, newStatus);
    }

    public List<ReplyDTO> getAllRepliesOf(int commentId) {
        String loginEmailSession = (String) session.getAttribute("email");
        int userId = userService.getIdByEmail(loginEmailSession);

        Comment comment = commentRepository.findById(commentId).orElseThrow();
        return comment.getReplies()
                .stream()
                .filter(reply -> reply.getStatus() == Status.ACTIVE)
                .filter(reply -> !userService.isBlockedBy(userId, reply.getReplier().getId()))
                .filter(reply -> !userService.isYouBeenBlockedBy(userId, reply.getReplier().getId()))
                .sorted(Comparator.comparing(Reply::getDateCreated))
                .map(this::convertToDTO)
                .toList();
    }

    public ReplyDTO getById(int replyId) {
        Reply reply = replyRepository.findById(replyId).orElseThrow(() -> new ResourceNotFoundException("Reply with id of " + replyId + " does not exists!"));
        return this.convertToDTO(reply);
    }

    public List<ReplyDTO> getAllUnreadReplyOf(int userId) {
        User user = userService.getById(userId);
        List<Comment> comments = user.getComments();

        return comments.stream()
                .map(Comment::getReplies)
                .flatMap(replies -> replies.stream()
                        .filter(reply -> reply.getStatus() == Status.ACTIVE)
                        .filter(reply -> !userService.isBlockedBy(userId, reply.getReplier().getId()))
                        .filter(reply -> !userService.isYouBeenBlockedBy(userId, reply.getReplier().getId()))
                        .filter(reply -> reply.getNotificationStatus() == NotificationStatus.UNREAD))
                .map(this::convertToDTO)
                .toList();
    }

    ReplyDTO convertToDTO(Reply reply) {
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
                .notificationStatus(reply.getNotificationStatus().name())
                .build();
    }

    public void setStatus(int replyId) {
        Reply reply = replyRepository.findById(replyId).orElseThrow(() -> new ResourceNotFoundException("Reply with id of " + replyId + " does not exists!"));
        reply.setStatus(Status.INACTIVE);
        replyRepository.save(reply);
    }
}
