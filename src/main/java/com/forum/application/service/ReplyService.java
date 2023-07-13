package com.forum.application.service;

import com.forum.application.dto.ReplyDTO;
import com.forum.application.exception.NoLoggedInUserException;
import com.forum.application.exception.ResourceNotFoundException;
import com.forum.application.mapper.ReplyMapper;
import com.forum.application.model.*;
import com.forum.application.repository.CommentRepository;
import com.forum.application.repository.ReplyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class ReplyService {
    private final UserService userService;
    private final CommentRepository commentRepository;
    private final ReplyRepository replyRepository;
    private final ReplyMapper replyMapper;

    int save(int replierId, int commentId, String body) throws ResourceNotFoundException {
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

    void delete(int replyId) {
        this.setStatus(replyId);
        log.debug("Reply with id of {} are now inactive!", replyId);
    }

    void updateReplyBody(int replyId, String newReplyBody) throws ResourceNotFoundException {
        Reply reply = replyRepository.findById(replyId).orElseThrow(() -> new ResourceNotFoundException("Reply with id of " + replyId + " does not exists!"));
        if (reply.getBody().equals(newReplyBody)) return;
        reply.setBody(newReplyBody);
        replyRepository.save(reply);
        log.debug("Reply with id of {} updated with the new body of {}", replyId, newReplyBody);
    }

    private void readReply(int replyId) throws ResourceNotFoundException {
        Reply reply = replyRepository.findById(replyId).orElseThrow(() -> new ResourceNotFoundException("Reply with id of " + replyId + " does not exists!"));
        reply.setNotificationStatus(NotificationStatus.READ);
        replyRepository.save(reply);
        log.debug("Reply with id of {} notification status updated successfully to {}", reply, NotificationStatus.READ);
    }

    public void readAllReplies(int commentId) throws ResourceNotFoundException, NoLoggedInUserException {
        int currentUserId = userService.getCurrentUser().getId();

        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new ResourceNotFoundException("Comment with id of " + commentId + " does not exists!"));
        if (currentUserId != comment.getCommenter().getId()) {
            log.trace("Will not mark as unread because the current user with id of {} are not the commenter of the comment {}", currentUserId, comment.getCommenter().getId());
            return;
        }
        log.trace("Will mark all as read because the current user with id of {} is the commenter of the comment {}", currentUserId, comment.getCommenter().getId());
        comment.getReplies()
                .stream()
                .filter(reply -> reply.getStatus() == Status.ACTIVE)
                .filter(reply -> !userService.isBlockedBy(currentUserId, reply.getReplier().getId()))
                .filter(reply -> !userService.isYouBeenBlockedBy(currentUserId, reply.getReplier().getId()))
                .map(Reply::getId)
                .forEach(this::readReply);
    }

    List<ReplyDTO> getAllRepliesOf(int commentId) throws NoLoggedInUserException, ResourceNotFoundException {
        int currentUserId = userService.getCurrentUser().getId();

        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new ResourceNotFoundException("Comment with id of " + commentId + " does not exists!"));
        return comment.getReplies()
                .stream()
                .filter(reply -> reply.getStatus() == Status.ACTIVE)
                .filter(reply -> !userService.isBlockedBy(currentUserId, reply.getReplier().getId()))
                .filter(reply -> !userService.isYouBeenBlockedBy(currentUserId, reply.getReplier().getId()))
                .sorted(Comparator.comparing(Reply::getDateCreated))
                .map(replyMapper::toDTO)
                .toList();
    }

    public ReplyDTO getById(int replyId) throws ResourceNotFoundException {
        Reply reply = replyRepository.findById(replyId).orElseThrow(() -> new ResourceNotFoundException("Reply with id of " + replyId + " does not exists!"));
        return replyMapper.toDTO(reply);
    }

    List<ReplyDTO> getUnreadRepliesOfAllComments(int userId) throws ResourceNotFoundException {
        User user = userService.getById(userId);
        List<Comment> comments = user.getComments();

        return comments.stream()
                .map(Comment::getReplies)
                .flatMap(replies -> replies.stream()
                        .filter(reply -> reply.getStatus() == Status.ACTIVE)
                        .filter(reply -> !userService.isBlockedBy(userId, reply.getReplier().getId()))
                        .filter(reply -> !userService.isYouBeenBlockedBy(userId, reply.getReplier().getId()))
                        .filter(reply -> reply.getNotificationStatus() == NotificationStatus.UNREAD))
                .map(replyMapper::toDTO)
                .toList();
    }

    public List<ReplyDTO> getAllUnreadReplies(int commenterId, int commentId) throws ResourceNotFoundException {
        User commenter = userService.getById(commenterId);
        Comment comment = commenter.getComments().stream().filter(userComment -> userComment.getId() == commentId).findFirst().orElseThrow(() -> new ResourceNotFoundException("Commenter with id of " + commenterId + " does not have a comment with id of " + commentId));
        return comment.getReplies()
                .stream()
                .filter(reply -> reply.getStatus() == Status.ACTIVE)
                .filter(reply -> reply.getNotificationStatus() == NotificationStatus.UNREAD)
                .filter(reply -> !userService.isBlockedBy(commenterId, reply.getReplier().getId()))
                .filter(reply -> !userService.isYouBeenBlockedBy(commenterId, reply.getReplier().getId()))
                .map(replyMapper::toDTO)
                .toList();
    }

    public int getNotificationCountForRespondent(int commenterId, int commentId, int respondentId) throws ResourceNotFoundException {
        return (int) getAllUnreadReplies(commenterId, commentId)
                .stream()
                .filter(reply -> reply.getReplierId() == respondentId)
                .count();
    }

    public int getReplyNotificationCountForSpecificComment(int commenterId, int commentId) throws ResourceNotFoundException {
        return getAllUnreadReplies(commenterId, commentId).size();
    }

    void setStatus(int replyId) throws ResourceNotFoundException {
        Reply reply = replyRepository.findById(replyId).orElseThrow(() -> new ResourceNotFoundException("Reply with id of " + replyId + " does not exists!"));
        reply.setStatus(Status.INACTIVE);
        replyRepository.save(reply);
    }
}
