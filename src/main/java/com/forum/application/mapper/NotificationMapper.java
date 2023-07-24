package com.forum.application.mapper;

import com.forum.application.dto.NotificationResponse;
import com.forum.application.dto.ReplyNotification;
import com.forum.application.exception.ResourceNotFoundException;
import com.forum.application.model.Comment;
import com.forum.application.model.ModalTracker;
import com.forum.application.model.Post;
import com.forum.application.model.Reply;
import com.forum.application.service.CommentService;
import com.forum.application.service.Formatter;
import com.forum.application.service.ReplyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {
    private final CommentService commentService;
    private final ReplyService replyService;

    @Autowired
    @Lazy

    public NotificationMapper(CommentService commentService, ReplyService replyService) {
        this.commentService = commentService;
        this.replyService = replyService;
    }

    public NotificationResponse toCommentNotification(Comment comment) throws ResourceNotFoundException {
        Post post = comment.getPost();
        int count = commentService.getNotificationCountForRespondent(post.getAuthor().getId(), post.getId(), comment.getCommenter().getId());
        return NotificationResponse.builder()
                .id(post.getId())
                .message(comment.getCommenter().getName() + " commented in your post: " + "\"" + post.getBody() + "\"")
                .respondentPicture(comment.getCommenter().getPicture())
                .respondentId(comment.getCommenter().getId())
                .type(ModalTracker.Type.COMMENT)
                .notificationStatus(comment.getNotificationStatus().name())
                .count(count)
                .formattedTime(Formatter.formatTime(comment.getDateCreated()))
                .formattedDate(Formatter.formatDate(comment.getDateCreated()))
                .build();
    }

    public NotificationResponse toReplyNotification(Reply reply) throws ResourceNotFoundException {
        final Comment comment = reply.getComment();

        int count = replyService.getNotificationCountForRespondent(comment.getCommenter().getId(), comment.getId(), reply.getReplier().getId());
        return ReplyNotification.replyNotificationBuilder()
                .id(comment.getId())
                .message(reply.getReplier().getName() + " replied to your comment: " +  "\"" + comment.getBody() + "\"")
                .respondentPicture(reply.getReplier().getPicture())
                .respondentId(reply.getReplier().getId())
                .type(ModalTracker.Type.REPLY)
                .count(count)
                .notificationStatus(reply.getNotificationStatus().name())
                .formattedDate(Formatter.formatDate(reply.getDateCreated()))
                .formattedTime(Formatter.formatTime(reply.getDateCreated()))
                .postId(comment.getPost().getId())
                .build();
    }
}
