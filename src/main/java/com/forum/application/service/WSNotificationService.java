package com.forum.application.service;

import com.forum.application.exception.ResourceNotFoundException;
import com.forum.application.mapper.NotificationMapper;
import com.forum.application.model.Comment;
import com.forum.application.model.Reply;
import com.forum.application.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class WSNotificationService {
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final NotificationMapper notificationMapper;

    void broadcastCommentNotification(Comment comment) throws ResourceNotFoundException {
        var commentNotificationResponse = notificationMapper.toCommentNotification(comment);

        int authorId = comment.getPost().getAuthor().getId();
        final String subscriberId = String.valueOf(authorId);
        simpMessagingTemplate.convertAndSendToUser(subscriberId, "/notification/comments", commentNotificationResponse);

        log.debug("Comment notification successfully sent to author with id of {}", subscriberId);
    }

    void broadcastReplyNotification(Reply reply) throws ResourceNotFoundException {
        var replyNotificationResponse = notificationMapper.toReplyNotification(reply);

        int commenterId = reply.getComment().getCommenter().getId();
        final String subscriberId = String.valueOf(commenterId);
        simpMessagingTemplate.convertAndSendToUser(subscriberId, "/notification/replies", replyNotificationResponse);

        log.debug("Reply notification successfully sent to commenter with id of {}", subscriberId);
    }
}
