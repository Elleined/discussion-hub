package com.forum.application.service;

import com.forum.application.dto.NotificationResponse;
import com.forum.application.exception.ResourceNotFoundException;
import com.forum.application.mapper.NotificationMapper;
import com.forum.application.model.Comment;
import com.forum.application.model.NotificationStatus;
import com.forum.application.model.Reply;
import com.forum.application.model.User;
import com.forum.application.model.mention.Mention;
import com.forum.application.model.mention.PostMention;
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

    void broadcastCommentNotification(Comment comment, User currentUser) throws ResourceNotFoundException {
        if (comment.getNotificationStatus() == NotificationStatus.READ) return; // If the post author replied in his own post it will not generate a notification block
        if (comment.getCommenter() == currentUser) return; // If the comment author modal is open this will not generate a notification block

        var commentNotificationResponse = notificationMapper.toCommentNotification(comment);
        int authorId = comment.getPost().getAuthor().getId();
        final String subscriberId = String.valueOf(authorId);
        simpMessagingTemplate.convertAndSendToUser(subscriberId, "/notification/comments", commentNotificationResponse);

        log.debug("Comment notification successfully sent to author with id of {}", subscriberId);
    }

    void broadcastReplyNotification(Reply reply, User currentUser) throws ResourceNotFoundException {
        if (reply.getNotificationStatus() == NotificationStatus.READ) return;
        if (reply.getReplier() == currentUser) return;

        var replyNotificationResponse = notificationMapper.toReplyNotification(reply);
        int commenterId = reply.getComment().getCommenter().getId();
        final String subscriberId = String.valueOf(commenterId);
        simpMessagingTemplate.convertAndSendToUser(subscriberId, "/notification/replies", replyNotificationResponse);

        log.debug("Reply notification successfully sent to commenter with id of {}", subscriberId);
    }
//
//    void broadcastMention(Mention mention) {
//        if (mention.getNotificationStatus() == NotificationStatus.READ) return;
//
//        NotificationResponse mentionNotification = switch (mention) {
//            case PostMention postMention -> notificationMapper.to
//
//        };
//
//        final int mentionedUserId = mention.getMentionedUser().getId();
//        final String subscriberId = String.valueOf(mentionedUserId);
//        simpMessagingTemplate.convertAndSendToUser(subscriberId, "/notification/mentions", mentionNotification );
//    }
}
