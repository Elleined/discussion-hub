package com.forum.application.service;

import com.forum.application.dto.CommentDTO;
import com.forum.application.dto.PostDTO;
import com.forum.application.dto.notification.CommentNotificationResponse;
import com.forum.application.dto.notification.ReplyNotificationResponse;
import com.forum.application.model.Type;
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
    private final ReplyService replyService;

    public void broadcastCommentNotification(int postId, int commenterId) {
        final PostDTO postDTO = postService.getById(postId);
        final User commenter = userService.getById(commenterId);

        boolean isModalOpen = userService.isModalOpen(postDTO.getAuthorId(), postId, Type.COMMENT);
        int count = commentService.getNotificationCountForRespondent(postDTO.getAuthorId(), postId, commenterId);
        var commentNotificationResponse = CommentNotificationResponse.builder()
                .message(commenter.getName() + " commented in your post: " + "\"" + postDTO.getBody() + "\"")
                .respondentPicture(commenter.getPicture())
                .respondentId(commenterId)
                .uri("/posts/" + postId + "/comments")
                .type(Type.COMMENT)
                .isModalOpen(isModalOpen)
                .count(count)
                .build();

        final String subscriberId = String.valueOf(postDTO.getAuthorId());
        simpMessagingTemplate.convertAndSendToUser(subscriberId, "/notification/comments", commentNotificationResponse);

        log.debug("Comment notification successfully sent to {}", subscriberId);
    }

    public void broadcastReplyNotification(int commentId, int replierId) {
        final CommentDTO commentDTO = commentService.getById(commentId);
        final User replier = userService.getById(replierId);

        boolean isModalOpen = userService.isModalOpen(commentDTO.getCommenterId(), commentId, Type.REPLY);
        int count = replyService.getNotificationCountForRespondent(commentDTO.getCommenterId(), commentId, replierId);
        var replyNotificationResponse = ReplyNotificationResponse.replyNotificationBuilder()
                .message(replier.getName() + " replied to your comment: " +  "\"" + commentDTO.getBody() + "\"")
                .respondentPicture(replier.getPicture())
                .respondentId(replierId)
                .uri("/posts/comments/" + commentId + "/replies")
                .commentURI("/posts/" + commentDTO.getPostId() + "/comments")
                .type(Type.REPLY)
                .count(count)
                .isModalOpen(isModalOpen)
                .build();

        final String subscriberId = String.valueOf(commentDTO.getCommenterId());
        simpMessagingTemplate.convertAndSendToUser(subscriberId, "/notification/replies", replyNotificationResponse);

        log.debug("Reply notification successfully sent to {}", subscriberId);
    }
}
