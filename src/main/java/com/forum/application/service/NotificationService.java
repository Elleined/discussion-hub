package com.forum.application.service;

import com.forum.application.dto.CommentDTO;
import com.forum.application.dto.MentionDTO;
import com.forum.application.dto.PostDTO;
import com.forum.application.dto.notification.CommentNotificationResponse;
import com.forum.application.dto.notification.NotificationResponse;
import com.forum.application.dto.notification.ReplyNotificationResponse;
import com.forum.application.exception.ResourceNotFoundException;
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
    private final MentionService mentionService;

    void broadcastCommentNotification(int postId, int commenterId) throws ResourceNotFoundException {
        var commentNotificationResponse = convertToCommentNotification(postId, commenterId);

        int authorId = postService.getById(postId).getAuthorId();
        final String subscriberId = String.valueOf(authorId);
        simpMessagingTemplate.convertAndSendToUser(subscriberId, "/notification/comments", commentNotificationResponse);

        log.debug("Comment notification successfully sent to {}", subscriberId);
    }

    void broadcastReplyNotification(int commentId, int replierId) throws ResourceNotFoundException {
        var replyNotificationResponse = convertToReplyNotification(commentId, replierId);

        int commenterId = commentService.getById(commentId).getCommenterId();
        final String subscriberId = String.valueOf(commenterId);
        simpMessagingTemplate.convertAndSendToUser(subscriberId, "/notification/replies", replyNotificationResponse);

        log.debug("Reply notification successfully sent to {}", subscriberId);
    }

    void broadcastMentionNotification(int mentionId) throws ResourceNotFoundException {
        MentionDTO mentionDTO = mentionService.toDTO(mentionService.getById(mentionId));
        User mentioningUser = userService.getById(mentionDTO.getMentioningUserId());
        String message = switch (Type.valueOf(mentionDTO.getType())) {
            case POST -> mentioningUser.getName() + " mention you in a post: " + "\"" + postService.getById(mentionDTO.getTypeId()).getBody() + "\"";
            case COMMENT -> mentioningUser.getName() + " mention you in a comment " + "\"" + commentService.getById(mentionDTO.getTypeId()).getBody() + "\"";
            case REPLY -> mentioningUser.getName() + " mention you in a reply " + "\"" + replyService.getById(mentionDTO.getTypeId()).getBody() + "\"";
        };
        mentionDTO.setMessage(message);
        final String subscriberId = String.valueOf(mentionDTO.getMentionedUserId());
        simpMessagingTemplate.convertAndSendToUser(subscriberId, "/notification/mentions", mentionDTO);
    }

    long getAllUnreadNotificationCount(int userId) throws ResourceNotFoundException {
        return commentService.getAllUnreadCommentsCount(userId) + replyService.getAllUnreadRepliesCount(userId);
        // return commentService.getAllUnreadCommentsCount(userId) + replyService.getAllUnreadRepliesCount(userId) + mentionService.getAllUnreadReceiveMentions(userId).size();
    }

    Set<NotificationResponse> getAllNotification(int userId) throws ResourceNotFoundException {
        List<NotificationResponse> commentNotifications = commentService.getAllUnreadCommentOfAllPostByAuthorId(userId)
                .stream()
                .map(comment -> convertToCommentNotification(comment.getPostId(), comment.getCommenterId()))
                .toList();

        List<NotificationResponse> replyNotifications = replyService.getAllUnreadRepliesOfAllCommentsByAuthorId(userId)
                .stream()
                .map(reply -> convertToReplyNotification(reply.getCommentId(), reply.getReplierId()))
                .toList();

        return Stream.of(commentNotifications, replyNotifications)
                .flatMap(notificationResponses -> notificationResponses.stream()
                        .sorted(Comparator.comparingInt(NotificationResponse::getRespondentId)))
                .collect(Collectors.toSet());
    }

    NotificationResponse convertToCommentNotification(int postId, int commenterId) throws ResourceNotFoundException {
        final PostDTO postDTO = postService.getById(postId);
        final User commenter = userService.getById(commenterId);

        boolean isModalOpen = userService.isModalOpen(postDTO.getAuthorId(), postId, Type.COMMENT);
        int count = commentService.getNotificationCountForRespondent(postDTO.getAuthorId(), postId, commenterId);
        return CommentNotificationResponse.builder()
                .id(postId)
                .message(commenter.getName() + " commented in your post: " + "\"" + postDTO.getBody() + "\"")
                .respondentPicture(commenter.getPicture())
                .respondentId(commenterId)
                .uri("/posts/" + postId + "/comments")
                .type(Type.COMMENT)
                .isModalOpen(isModalOpen)
                .count(count)
                .build();
    }

    NotificationResponse convertToReplyNotification(int commentId, int replierId) throws ResourceNotFoundException {
        final CommentDTO commentDTO = commentService.getById(commentId);
        final User replier = userService.getById(replierId);

        boolean isModalOpen = userService.isModalOpen(commentDTO.getCommenterId(), commentId, Type.REPLY);
        int count = replyService.getNotificationCountForRespondent(commentDTO.getCommenterId(), commentId, replierId);
        return ReplyNotificationResponse.replyNotificationBuilder()
                .id(commentId)
                .message(replier.getName() + " replied to your comment: " +  "\"" + commentDTO.getBody() + "\"")
                .respondentPicture(replier.getPicture())
                .respondentId(replierId)
                .uri("/posts/comments/" + commentId + "/replies")
                .commentURI("/posts/" + commentDTO.getPostId() + "/comments")
                .type(Type.REPLY)
                .count(count)
                .isModalOpen(isModalOpen)
                .build();
    }
}
