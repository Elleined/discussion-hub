package com.forum.application.mapper;

import com.forum.application.dto.NotificationResponse;
import com.forum.application.dto.ReplyNotification;
import com.forum.application.model.Comment;
import com.forum.application.model.ModalTracker;
import com.forum.application.model.Reply;
import com.forum.application.model.like.CommentLike;
import com.forum.application.model.like.PostLike;
import com.forum.application.model.like.ReplyLike;
import com.forum.application.service.CommentService;
import com.forum.application.service.Formatter;
import com.forum.application.service.ReplyService;
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
            @Mapping(target = "count", expression = "java(commentService.getNotificationCountForRespondent(comment.getPost().getAuthor(), comment.getPost().getId(), comment.getCommenter().getId()))"),
    })
    public abstract NotificationResponse toCommentNotification(Comment comment);

    @Mappings(value = {
            @Mapping(target = "id", source = "reply.id"),
            @Mapping(target = "message", expression = "java(getReplyMessage(reply))"),
            @Mapping(target = "respondentId", source = "reply.replier.id"),
            @Mapping(target = "respondentPicture", source = "reply.replier.picture"),
            @Mapping(target = "formattedDate", expression = "java(Formatter.formatDate(reply.getDateCreated()))"),
            @Mapping(target = "formattedTime", expression = "java(Formatter.formatTime(reply.getDateCreated()))"),
            @Mapping(target = "notificationStatus", source = "reply.notificationStatus"),
            @Mapping(target = "type", expression = "java(Type.REPLY)"),
            @Mapping(target = "count", expression = "java(replyService.getNotificationCountForRespondent(reply.getComment().getCommenter(), reply.getComment().getId(), reply.getReplier().getId()))"),
            @Mapping(target = "postId", source = "reply.comment.post.id")
    })
    public abstract ReplyNotification toReplyNotification(Reply reply);

    @Mappings(value = {
            @Mapping(target = "id", source = "postLike.id"),
            @Mapping(target = "message", expression = "java(getPostLikeMessage(postLike))"),
            @Mapping(target = "respondentId", source = "postLike.respondent.id"),
            @Mapping(target = "respondentPicture", source = "postLike.respondent.picture"),
            @Mapping(target = "formattedDate", expression = "java(Formatter.formatDate(postLike.getCreatedAt()))"),
            @Mapping(target = "formattedTime", expression = "java(Formatter.formatTime(postLike.getCreatedAt()))"),
            @Mapping(target = "notificationStatus", source = "postLike.notificationStatus"),
            @Mapping(target = "type", expression = "java(Type.POST)"),
            @Mapping(target = "count", ignore = true) // not yet implemented
    })
    public abstract NotificationResponse toPostLikeNotification(PostLike postLike);

    @Mappings(value = {
            @Mapping(target = "id", source = "commentLike.id"),
            @Mapping(target = "message", expression = "java(getCommentLikeMessage(commentLike))"),
            @Mapping(target = "respondentId", source = "commentLike.respondent.id"),
            @Mapping(target = "respondentPicture", source = "commentLike.respondent.picture"),
            @Mapping(target = "formattedDate", expression = "java(Formatter.formatDate(commentLike.getCreatedAt()))"),
            @Mapping(target = "formattedTime", expression = "java(Formatter.formatTime(commentLike.getCreatedAt()))"),
            @Mapping(target = "notificationStatus", source = "commentLike.notificationStatus"),
            @Mapping(target = "type", expression = "java(Type.COMMENT)"),
            @Mapping(target = "count", ignore = true) // not yet implemented
    })
    public abstract NotificationResponse toCommentLikeNotification(CommentLike commentLike);

    @Mappings(value = {
            @Mapping(target = "id", source = "replyLike.id"),
            @Mapping(target = "message", expression = "java(getReplyLikeMessage(replyLike))"),
            @Mapping(target = "respondentId", source = "replyLike.respondent.id"),
            @Mapping(target = "respondentPicture", source = "replyLike.respondent.picture"),
            @Mapping(target = "formattedDate", expression = "java(Formatter.formatDate(replyLike.getCreatedAt()))"),
            @Mapping(target = "formattedTime", expression = "java(Formatter.formatTime(replyLike.getCreatedAt()))"),
            @Mapping(target = "notificationStatus", source = "replyLike.notificationStatus"),
            @Mapping(target = "type", expression = "java(Type.REPLY)"),
            @Mapping(target = "count", ignore = true),
            @Mapping(target = "postId", source = "replyLike.reply.comment.post.id")
    })
    public abstract ReplyNotification toReplyLikeNotification(ReplyLike replyLike);

    protected String getCommentMessage(Comment comment) {
        return comment.getCommenter().getName() + " commented in your post: " + "\"" + comment.getPost().getBody() + "\"";
    }

    protected String getReplyMessage(Reply reply) {
        return reply.getReplier().getName() + " replied to your comment: " + "\"" + reply.getComment().getBody() + "\"";
    }

    protected String getPostLikeMessage(PostLike postLike) {
        return postLike.getRespondent().getName() + " liked your post: " +  "\"" + postLike.getPost().getBody() + "\"";
    }

    protected String getCommentLikeMessage(CommentLike commentLike) {
        return commentLike.getRespondent().getName() + " liked your comment: " +  "\"" + commentLike.getComment().getBody() + "\"";
    }

    protected String getReplyLikeMessage(ReplyLike replyLike) {
        return replyLike.getRespondent().getName() + " liked your reply: " +  "\"" + replyLike.getReply().getBody() + "\"";
    }
}

