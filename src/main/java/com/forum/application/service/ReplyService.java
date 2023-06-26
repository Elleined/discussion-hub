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

        NotificationStatus status = userService.isModalOpen(comment.getCommenter().getId(), commentId, Type.REPLY) ? NotificationStatus.READ : NotificationStatus.UNREAD;
        Reply reply = Reply.builder()
                .body(body)
                .dateCreated(LocalDateTime.now())
                .replier(replier)
                .comment(comment)
                .status(Status.ACTIVE)
                .notificationStatus(status)
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

    private void updateNotificationStatus(int replyId, NotificationStatus newStatus) {
        Reply reply = replyRepository.findById(replyId).orElseThrow(() -> new ResourceNotFoundException("Reply with id of " + replyId + " does not exists!"));
        reply.setNotificationStatus(newStatus);
        replyRepository.save(reply);
        log.debug("Reply with id of {} notification status updated successfully to {}", reply, newStatus);
    }

    public void updateAllRepliesByCommentId(int commentId, NotificationStatus newStatus) {
        String loginEmailSession = (String) session.getAttribute("email");
        int userId = userService.getIdByEmail(loginEmailSession);

        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new ResourceNotFoundException("Comment with id of " + commentId + " does not exists!"));
        if (userId != comment.getCommenter().getId()) {
            log.trace("Will not mark as unread because the current user with id of {} are not the commenter of the comment {}", userId, comment.getCommenter().getId());
            return;
        }
        log.trace("Will mark all as read because the current user with id of {} is the commenter of the comment {}", userId, comment.getCommenter().getId());

        comment.getReplies()
                .stream()
                .filter(reply -> reply.getStatus() == Status.ACTIVE)
                .filter(reply -> !userService.isBlockedBy(userId, reply.getReplier().getId()))
                .filter(reply -> !userService.isYouBeenBlockedBy(userId, reply.getReplier().getId()))
                .map(Reply::getId)
                .forEach(replyId -> this.updateNotificationStatus(replyId, newStatus));
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

    public List<ReplyDTO> getAllUnreadReplyOf(int commenterId, int commentId) {
        User commenter = userService.getById(commenterId);
        Comment comment = commenter.getComments().stream().filter(userComment -> userComment.getId() == commentId).findFirst().orElseThrow(() -> new ResourceNotFoundException("Commenter with id of " + commenterId + " does not have a comment with id of " + commentId));
        return comment.getReplies()
                .stream()
                .filter(reply -> reply.getStatus() == Status.ACTIVE)
                .filter(reply -> reply.getNotificationStatus() == NotificationStatus.UNREAD)
                .filter(reply -> !userService.isBlockedBy(commenterId, reply.getReplier().getId()))
                .filter(reply -> !userService.isYouBeenBlockedBy(commenterId, reply.getReplier().getId()))
                .map(this::convertToDTO)
                .toList();
    }

    public int getNotificationCountForRespondent(int commenterId, int commentId, int respondentId) {
        return (int) getAllUnreadReplyOf(commenterId, commentId)
                .stream()
                .filter(reply -> reply.getReplierId() == respondentId)
                .count();
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

    void setStatus(int replyId) {
        Reply reply = replyRepository.findById(replyId).orElseThrow(() -> new ResourceNotFoundException("Reply with id of " + replyId + " does not exists!"));
        reply.setStatus(Status.INACTIVE);
        replyRepository.save(reply);
    }
}
