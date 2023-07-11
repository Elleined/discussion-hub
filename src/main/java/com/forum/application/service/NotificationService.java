package com.forum.application.service;

import com.forum.application.dto.MentionResponse;
import com.forum.application.dto.notification.NotificationResponse;
import com.forum.application.exception.ResourceNotFoundException;
import com.forum.application.mapper.NotificationMapper;
import com.forum.application.model.Type;
import com.forum.application.model.User;
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
    private final UserService userService;
    private final PostService postService;
    private final CommentService commentService;
    private final ReplyService replyService;
    private final NotificationMapper notificationMapper;
    private final MentionService mentionService;
    void broadcastCommentNotification(int postId, int commenterId) throws ResourceNotFoundException {
        var commentNotificationResponse = notificationMapper.toCommentNotification(postId, commenterId);

        int authorId = postService.getById(postId).getAuthorId();
        final String subscriberId = String.valueOf(authorId);
        simpMessagingTemplate.convertAndSendToUser(subscriberId, "/notification/comments", commentNotificationResponse);

        log.debug("Comment notification successfully sent to {}", subscriberId);
    }

    void broadcastReplyNotification(int commentId, int replierId) throws ResourceNotFoundException {
        var replyNotificationResponse = notificationMapper.toReplyNotification(commentId, replierId);

        int commenterId = commentService.getById(commentId).getCommenterId();
        final String subscriberId = String.valueOf(commenterId);
        simpMessagingTemplate.convertAndSendToUser(subscriberId, "/notification/replies", replyNotificationResponse);

        log.debug("Reply notification successfully sent to {}", subscriberId);
    }

    void broadcastMentionNotification(MentionResponse mentionResponse) throws ResourceNotFoundException {
        User mentioningUser = userService.getById(mentionResponse.getMentioningUserId());
        String message = switch (Type.valueOf(mentionResponse.getType())) {
            case POST -> mentioningUser.getName() + " mention you in a post: " + "\"" + postService.getById(mentionResponse.getTypeId()).getBody() + "\"";
            case COMMENT -> mentioningUser.getName() + " mention you in a comment " + "\"" + commentService.getById(mentionResponse.getTypeId()).getBody() + "\"";
            case REPLY -> mentioningUser.getName() + " mention you in a reply " + "\"" + replyService.getById(mentionResponse.getTypeId()).getBody() + "\"";
        };
        mentionResponse.setMessage(message);
        final String subscriberId = String.valueOf(mentionResponse.getMentionedUserId());
        simpMessagingTemplate.convertAndSendToUser(subscriberId, "/notification/mentions", mentionResponse);
    }

    long getAllUnreadNotificationCount(int userId) throws ResourceNotFoundException {
        return commentService.getAllUnreadCommentOfAllPostByAuthorId(userId).size() +
                replyService.getAllUnreadRepliesOfAllCommentsByAuthorId(userId).size() +
                mentionService.getAllUnreadReceiveMentions(userId).size();
    }

    Set<NotificationResponse> getAllNotification(int userId) throws ResourceNotFoundException {
        List<NotificationResponse> commentNotifications = commentService.getAllUnreadCommentOfAllPostByAuthorId(userId)
                .stream()
                .map(comment -> notificationMapper.toCommentNotification(comment.getPostId(), comment.getCommenterId()))
                .toList();

        List<NotificationResponse> replyNotifications = replyService.getAllUnreadRepliesOfAllCommentsByAuthorId(userId)
                .stream()
                .map(reply -> notificationMapper.toReplyNotification(reply.getCommentId(), reply.getReplierId()))
                .toList();

        return Stream.of(commentNotifications, replyNotifications)
                .flatMap(notificationResponses -> notificationResponses.stream()
                        .sorted(Comparator.comparingInt(NotificationResponse::getRespondentId)))
                .collect(Collectors.toSet());
    }
}
