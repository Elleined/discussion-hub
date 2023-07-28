package com.forum.application.mapper;

import com.forum.application.dto.NotificationResponse;
import com.forum.application.model.Comment;
import com.forum.application.model.ModalTracker;
import com.forum.application.model.Reply;
import com.forum.application.model.like.CommentLike;
import com.forum.application.model.like.Like;
import com.forum.application.model.like.PostLike;
import com.forum.application.model.like.ReplyLike;
import com.forum.application.model.mention.CommentMention;
import com.forum.application.model.mention.Mention;
import com.forum.application.model.mention.PostMention;
import com.forum.application.model.mention.ReplyMention;
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
            @Mapping(target = "postId", source = "comment.post.id"),
            @Mapping(target = "commentId", ignore = true)
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
            @Mapping(target = "postId", source = "reply.comment.post.id"),
            @Mapping(target = "commentId", source = "reply.comment.id")
    })
    public abstract NotificationResponse toReplyNotification(Reply reply);

    @Mappings(value = {
            @Mapping(target = "id", source = "postLike.id"),
            @Mapping(target = "message", expression = "java(getLikeMessage(postLike))"),
            @Mapping(target = "respondentId", source = "postLike.respondent.id"),
            @Mapping(target = "respondentPicture", source = "postLike.respondent.picture"),
            @Mapping(target = "formattedDate", expression = "java(Formatter.formatDate(postLike.getCreatedAt()))"),
            @Mapping(target = "formattedTime", expression = "java(Formatter.formatTime(postLike.getCreatedAt()))"),
            @Mapping(target = "notificationStatus", source = "postLike.notificationStatus"),
            @Mapping(target = "type", expression = "java(Type.POST)"),
            @Mapping(target = "count", ignore = true),
            @Mapping(target = "commentId", ignore = true),
            @Mapping(target = "postId", ignore = true)
    })
    public abstract NotificationResponse toPostLikeNotification(PostLike postLike);

    @Mappings(value = {
            @Mapping(target = "id", source = "commentLike.id"),
            @Mapping(target = "message", expression = "java(getLikeMessage(commentLike))"),
            @Mapping(target = "respondentId", source = "commentLike.respondent.id"),
            @Mapping(target = "respondentPicture", source = "commentLike.respondent.picture"),
            @Mapping(target = "formattedDate", expression = "java(Formatter.formatDate(commentLike.getCreatedAt()))"),
            @Mapping(target = "formattedTime", expression = "java(Formatter.formatTime(commentLike.getCreatedAt()))"),
            @Mapping(target = "notificationStatus", source = "commentLike.notificationStatus"),
            @Mapping(target = "postId", source = "commentLike.comment.post.id"),
            @Mapping(target = "type", expression = "java(Type.COMMENT)"),
            @Mapping(target = "count", ignore = true),
            @Mapping(target = "commentId", ignore = true)
    })
    public abstract NotificationResponse toCommentLikeNotification(CommentLike commentLike);

    @Mappings(value = {
            @Mapping(target = "id", source = "replyLike.id"),
            @Mapping(target = "message", expression = "java(getLikeMessage(replyLike))"),
            @Mapping(target = "respondentId", source = "replyLike.respondent.id"),
            @Mapping(target = "respondentPicture", source = "replyLike.respondent.picture"),
            @Mapping(target = "formattedDate", expression = "java(Formatter.formatDate(replyLike.getCreatedAt()))"),
            @Mapping(target = "formattedTime", expression = "java(Formatter.formatTime(replyLike.getCreatedAt()))"),
            @Mapping(target = "notificationStatus", source = "replyLike.notificationStatus"),
            @Mapping(target = "type", expression = "java(Type.REPLY)"),
            @Mapping(target = "count", ignore = true),
            @Mapping(target = "postId", source = "replyLike.reply.comment.post.id"),
            @Mapping(target = "commentId", source = "replyLike.reply.comment.id")
    })
    public abstract NotificationResponse toReplyLikeNotification(ReplyLike replyLike);

    @Mappings({
            @Mapping(target = "id", source = "postMention.id"),
            @Mapping(target = "message", expression = "java(getMentionMessage(postMention))"),
            @Mapping(target = "respondentId", source = "postMention.mentioningUser.id"),
            @Mapping(target = "respondentPicture", source = "postMention.mentioningUser.picture"),
            @Mapping(target = "formattedDate", expression = "java(Formatter.formatDate(postMention.getCreatedAt()))"),
            @Mapping(target = "formattedTime", expression = "java(Formatter.formatTime(postMention.getCreatedAt()))"),
            @Mapping(target = "type", expression = "java(Type.COMMENT)"),
            @Mapping(target = "notificationStatus", source = "postMention.notificationStatus"),
            @Mapping(target = "postId", source = "postMention.post.id"),
            @Mapping(target = "commentId", ignore = true),
            @Mapping(target = "count", ignore = true),
    })
    public abstract NotificationResponse toPostMentionNotification(PostMention postMention);

    @Mappings({
            @Mapping(target = "id", source = "commentMention.id"),
            @Mapping(target = "message", expression = "java(getMentionMessage(commentMention))"),
            @Mapping(target = "respondentId", source = "commentMention.mentioningUser.id"),
            @Mapping(target = "respondentPicture", source = "commentMention.mentioningUser.picture"),
            @Mapping(target = "formattedDate", expression = "java(Formatter.formatDate(commentMention.getCreatedAt()))"),
            @Mapping(target = "formattedTime", expression = "java(Formatter.formatTime(commentMention.getCreatedAt()))"),
            @Mapping(target = "type", expression = "java(Type.COMMENT)"),
            @Mapping(target = "postId", source = "commentMention.comment.post.id"),
            @Mapping(target = "notificationStatus", source = "commentMention.notificationStatus"),
            @Mapping(target = "commentId", ignore = true),
            @Mapping(target = "count", ignore = true),
    })
    public abstract NotificationResponse toCommentMentionNotification(CommentMention commentMention);

    @Mappings({
            @Mapping(target = "id", source = "replyMention.id"),
            @Mapping(target = "message", expression = "java(getMentionMessage(replyMention))"),
            @Mapping(target = "respondentId", source = "replyMention.mentioningUser.id"),
            @Mapping(target = "respondentPicture", source = "replyMention.mentioningUser.picture"),
            @Mapping(target = "formattedDate", expression = "java(Formatter.formatDate(replyMention.getCreatedAt()))"),
            @Mapping(target = "formattedTime", expression = "java(Formatter.formatTime(replyMention.getCreatedAt()))"),
            @Mapping(target = "type", expression = "java(Type.REPLY)"),
            @Mapping(target = "postId", source = "replyMention.reply.comment.post.id"),
            @Mapping(target = "commentId", source = "replyMention.reply.comment.id"),
            @Mapping(target = "notificationStatus", source = "notificationStatus"),
            @Mapping(target = "count", ignore = true),
    })
    public abstract NotificationResponse toReplyMentionNotification(ReplyMention replyMention);

    protected String getCommentMessage(Comment comment) {
        return comment.getCommenter().getName() + " commented in your post: " + "\"" + comment.getPost().getBody() + "\"";
    }

    protected String getReplyMessage(Reply reply) {
        return reply.getReplier().getName() + " replied to your comment: " + "\"" + reply.getComment().getBody() + "\"";
    }

    protected String getLikeMessage(Like like) {
        return switch (like) {
            case PostLike postLike-> postLike.getRespondent().getName() + " liked your post: " +  "\"" + postLike.getPost().getBody() + "\"";
            case CommentLike commentLike-> commentLike.getRespondent().getName() + " liked your comment: " +  "\"" + commentLike.getComment().getBody() + "\"";
            case ReplyLike replyLike -> replyLike.getRespondent().getName() + " liked your reply: " +  "\"" + replyLike.getReply().getBody() + "\"";
            default -> throw new IllegalStateException("Unexpected value: " + like);
        };
    }

    protected String getMentionMessage(Mention mention) {
        return switch (mention) {
            case PostMention postMention -> postMention.getMentioningUser().getName() + " mentioned you in a post: " + "\"" + postMention.getPost().getBody() + "\"";
            case CommentMention commentMention -> commentMention.getMentioningUser().getName() + " mentioned you in a comment: " + "\"" + commentMention.getComment().getBody() + "\"";
            case ReplyMention replyMention -> replyMention.getMentioningUser().getName() + " mentioned you in a reply: " + "\"" + replyMention.getReply().getBody() + "\"";
            default -> throw new IllegalStateException("Unexpected value: " + mention);
        };
    }
}

