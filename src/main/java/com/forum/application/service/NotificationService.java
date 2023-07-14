package com.forum.application.service;

import com.forum.application.dto.NotificationResponse;
import com.forum.application.exception.ResourceNotFoundException;
import com.forum.application.mapper.NotificationMapper;
import com.forum.application.model.Comment;
import com.forum.application.model.Mention;
import com.forum.application.model.Reply;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final CommentService commentService;
    private final ReplyService replyService;
    private final NotificationMapper notificationMapper;
    private final MentionService mentionService;

    void broadcastCommentNotification(Comment comment) throws ResourceNotFoundException {
        var commentNotificationResponse = notificationMapper.toCommentNotification(comment);

        int authorId = comment.getPost().getAuthor().getId();
        final String subscriberId = String.valueOf(authorId);
        simpMessagingTemplate.convertAndSendToUser(subscriberId, "/notification/comments", commentNotificationResponse);

        log.debug("Comment notification successfully sent to {}", subscriberId);
    }

    void broadcastReplyNotification(Reply reply) throws ResourceNotFoundException {
        var replyNotificationResponse = notificationMapper.toReplyNotification(reply);

        int commenterId = reply.getComment().getCommenter().getId();
        final String subscriberId = String.valueOf(commenterId);
        simpMessagingTemplate.convertAndSendToUser(subscriberId, "/notification/replies", replyNotificationResponse);

        log.debug("Reply notification successfully sent to {}", subscriberId);
    }

    void broadcastMentionNotification(int mentionId) throws ResourceNotFoundException {
        Mention mention = mentionService.getById(mentionId);
        var mentionResponse = notificationMapper.toMentionNotification(mention);
        final String subscriberId = String.valueOf(mention.getMentionedUser().getId());
        simpMessagingTemplate.convertAndSendToUser(subscriberId, "/notification/mentions", mentionResponse);
    }

    long getAllUnreadNotificationCount(int userId) throws ResourceNotFoundException {
        return commentService.getUnreadCommentsOfAllPost(userId).size() +
                replyService.getUnreadRepliesOfAllComments(userId).size() +
                mentionService.getAllUnreadReceiveMentions(userId).size();
    }

    public Set<NotificationResponse> getAllNotification(int userId) throws ResourceNotFoundException {
        List<NotificationResponse> commentNotifications = commentService.getUnreadCommentsOfAllPost(userId)
                .stream()
                .map(notificationMapper::toCommentNotification)
                .toList();

        List<NotificationResponse> replyNotifications = replyService.getUnreadRepliesOfAllComments(userId)
                .stream()
                .map(notificationMapper::toReplyNotification)
                .toList();

        return Stream.of(commentNotifications, replyNotifications)
                .flatMap(notificationResponses -> notificationResponses.stream()
                        .sorted(Comparator.comparing(NotificationResponse::getFormattedDate)))
                .collect(Collectors.toSet());
    }
}
