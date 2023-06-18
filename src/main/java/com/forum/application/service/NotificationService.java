package com.forum.application.service;

import com.forum.application.dto.CommentDTO;
import com.forum.application.dto.notification.CommentNotificationResponse;
import com.forum.application.dto.PostDTO;
import com.forum.application.dto.notification.NotificationResponse;
import com.forum.application.dto.notification.ReplyNotificationResponse;
import com.forum.application.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final UserService userService;
    private final PostService postService;
    private final CommentService commentService;

    public void broadcastCommentNotification(int postId, int commenterId) {
        final PostDTO postDTO = postService.getById(postId);
        final User commenter = userService.getById(commenterId);

        var commentNotificationResponse = CommentNotificationResponse.builder()
                .message(commenter.getName() + " commented in your post: " + "\"" + postDTO.getBody() + "\"")
                .respondentPicture(commenter.getPicture())
                .respondentId(commenterId)
                .uri("/posts/" + postId + "/comments")
                .type(NotificationResponse.Type.COMMENT)
                .build();

        final String destination = "/discussion/forum-notification/comments/" + postDTO.getAuthorId();
        simpMessagingTemplate.convertAndSend(destination, commentNotificationResponse);

        log.debug("Comment notification successfully sent to {}", destination);
    }

    public void broadcastReplyNotification(int commentId, int replierId) {
        final CommentDTO commentDTO = commentService.getById(commentId);
        final User replier = userService.getById(replierId);

        var replyNotificationResponse = ReplyNotificationResponse.builder()
                .message(replier.getName() + " replied to your comment: " + commentDTO.getBody())
                .respondentPicture(replier.getPicture())
                .respondentId(replierId)
                .uri("/posts/comments/" + commentId + "/replies")
                .type(NotificationResponse.Type.REPLY)
                .build();

        final String destination = "/discussion/forum-notification/replies/" + commentDTO.getCommenterId();
        simpMessagingTemplate.convertAndSend(destination, replyNotificationResponse);

        log.debug("Reply notification successfully sent to {}", destination);
    }
}
