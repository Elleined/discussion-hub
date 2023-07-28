package com.forum.application.service;

import com.forum.application.dto.NotificationResponse;
import com.forum.application.exception.ResourceNotFoundException;
import com.forum.application.mapper.NotificationMapper;
import com.forum.application.model.Comment;
import com.forum.application.model.NotificationStatus;
import com.forum.application.model.Reply;
import com.forum.application.model.like.CommentLike;
import com.forum.application.model.like.Like;
import com.forum.application.model.like.PostLike;
import com.forum.application.model.like.ReplyLike;
import com.forum.application.model.mention.CommentMention;
import com.forum.application.model.mention.Mention;
import com.forum.application.model.mention.PostMention;
import com.forum.application.model.mention.ReplyMention;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class WSNotificationService {
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final NotificationMapper notificationMapper;

    void broadcastCommentNotification(Comment comment) throws ResourceNotFoundException {
        if (comment.getNotificationStatus() == NotificationStatus.READ) return; // If the post author replied in his own post it will not generate a notification block

        var commentNotificationResponse = notificationMapper.toNotification(comment);
        int authorId = comment.getPost().getAuthor().getId();
        simpMessagingTemplate.convertAndSendToUser(String.valueOf(authorId), "/notification/comments", commentNotificationResponse);
        log.debug("Comment notification successfully sent to author with id of {}", authorId);
    }

    void broadcastReplyNotification(Reply reply) throws ResourceNotFoundException {
        if (reply.getNotificationStatus() == NotificationStatus.READ) return;

        var replyNotificationResponse = notificationMapper.toNotification(reply);
        int commenterId = reply.getComment().getCommenter().getId();
        simpMessagingTemplate.convertAndSendToUser(String.valueOf(commenterId), "/notification/replies", replyNotificationResponse);

        log.debug("Reply notification successfully sent to commenter with id of {}", commenterId);
    }

    void broadcastMentions(Set<Mention> mentions) {
        mentions.forEach(this::broadcastMention);
    }

    void broadcastLike(Like like) {
        if (like.getNotificationStatus() == NotificationStatus.READ) return;

        int subscriberId = switch (like) {
            case PostLike postLike -> postLike.getPost().getAuthor().getId();
            case CommentLike commentLike -> commentLike.getComment().getCommenter().getId();
            case ReplyLike replyLike  -> replyLike.getReply().getReplier().getId();
            default -> throw new IllegalStateException("Unexpected value: " + like);
        };

        NotificationResponse likeNotification = switch (like) {
            case PostLike postLike -> notificationMapper.toLikeNotification(postLike);
            case CommentLike commentLike -> notificationMapper.toLikeNotification(commentLike);
            case ReplyLike replyLike  -> notificationMapper.toLikeNotification(replyLike);
            default -> throw new IllegalStateException("Unexpected value: " + like);
        };
        simpMessagingTemplate.convertAndSendToUser(String.valueOf(subscriberId), "/notification/likes", likeNotification);
    }

    private void broadcastMention(Mention mention) {
        if (mention.getNotificationStatus() == NotificationStatus.READ) return;

        NotificationResponse mentionNotification = switch (mention) {
            case PostMention postMention -> notificationMapper.toMentionNotification(postMention);
            case CommentMention commentMention -> notificationMapper.toMentionNotification(commentMention);
            case ReplyMention replyMention -> notificationMapper.toMentionNotification(replyMention);
            default -> throw new IllegalStateException("Unexpected value: " + mention);
        };

        final int mentionedUserId = mention.getMentionedUser().getId();
        simpMessagingTemplate.convertAndSendToUser(String.valueOf(mentionedUserId), "/notification/mentions", mentionNotification);
    }
}
