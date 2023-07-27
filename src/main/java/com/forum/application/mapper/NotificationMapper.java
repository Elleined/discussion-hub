package com.forum.application.mapper;

import com.forum.application.dto.NotificationResponse;
import com.forum.application.dto.ReplyNotification;
import com.forum.application.model.Comment;
import com.forum.application.model.ModalTracker;
import com.forum.application.model.Reply;
import com.forum.application.model.User;
import com.forum.application.service.CommentService;
import com.forum.application.service.Formatter;
import com.forum.application.service.ReplyService;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

@Mapper(componentModel = "spring", imports = {Formatter.class, ModalTracker.Type.class})
public abstract class NotificationMapper {
    @Autowired
    @Lazy
    protected CommentService commentService;
    @Autowired @Lazy
    protected ReplyService replyService;

    @Mappings(value = {
            @Mapping(target = "id", source = "comment.id"),
            @Mapping(target = "message", expression = "java(getCommentMessage(comment))"),
            @Mapping(target = "respondentId", source = "comment.commenter.id"),
            @Mapping(target = "respondentPicture", source = "comment.commenter.picture"),
            @Mapping(target = "formattedDate", expression = "java(Formatter.formatDate(comment.getDateCreated()))"),
            @Mapping(target = "formattedTime", expression = "java(Formatter.formatTime(comment.getDateCreated()))"),
            @Mapping(target = "notificationStatus", source = "comment.notificationStatus"),
            @Mapping(target = "type", expression = "java(Type.COMMENT)"),
            @Mapping(target = "count", expression = "java(commentService.getNotificationCountForRespondent(currentUser, comment.getPost().getId(), comment.getCommenter().getId()))"),
    })
    public abstract NotificationResponse toCommentNotification(Comment comment, @Context User currentUser);

    @Mappings(value = {
            @Mapping(target = "id", source = "reply.id"),
            @Mapping(target = "message", expression = "java(getReplyMessage(reply))"),
            @Mapping(target = "respondentId", source = "reply.replier.id"),
            @Mapping(target = "respondentPicture", source = "reply.replier.picture"),
            @Mapping(target = "formattedDate", expression = "java(Formatter.formatDate(reply.getDateCreated()))"),
            @Mapping(target = "formattedTime", expression = "java(Formatter.formatTime(reply.getDateCreated()))"),
            @Mapping(target = "notificationStatus", source = "reply.notificationStatus"),
            @Mapping(target = "type", expression = "java(Type.REPLY)"),
            @Mapping(target = "count", expression = "java(replyService.getNotificationCountForRespondent(currentUser, reply.getComment().getId(), reply.getReplier().getId()))"),
            @Mapping(target = "postId", source = "reply.comment.post.id")
    })
    public abstract ReplyNotification toReplyNotification(Reply reply, @Context User currentUser);

    protected String getCommentMessage(Comment comment) {
        return comment.getCommenter().getName() + " commented in your post: " + "\"" + comment.getPost().getBody() + "\"";
    }

    protected String getReplyMessage(Reply reply) {
        return reply.getReplier().getName() + " replied to your comment: " +  "\"" + reply.getComment().getBody() + "\"";
    }
}

