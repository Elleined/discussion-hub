package com.forum.application.mapper;

import com.forum.application.dto.CommentDTO;
import com.forum.application.dto.PostDTO;
import com.forum.application.dto.NotificationResponse;
import com.forum.application.exception.ResourceNotFoundException;
import com.forum.application.model.Mention;
import com.forum.application.model.Type;
import com.forum.application.model.User;
import com.forum.application.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

    private final PostService postService;
    private final UserService userService;
    private final CommentService commentService;
    private final ReplyService replyService;

    @Autowired @Lazy
    public NotificationMapper(PostService postService, UserService userService, CommentService commentService, ReplyService replyService) {
        this.postService = postService;
        this.userService = userService;
        this.commentService = commentService;
        this.replyService = replyService;
    }

    public NotificationResponse toCommentNotification(int postId, int commenterId) throws ResourceNotFoundException {
        final PostDTO postDTO = postService.getById(postId);
        final User commenter = userService.getById(commenterId);

        boolean isModalOpen = userService.isModalOpen(postDTO.getAuthorId(), postId, Type.COMMENT);
        int count = commentService.getNotificationCountForRespondent(postDTO.getAuthorId(), postId, commenterId);
        return NotificationResponse.builder()
                .id(postId)
                .message(commenter.getName() + " commented in your post: " + "\"" + postDTO.getBody() + "\"")
                .respondentPicture(commenter.getPicture())
                .respondentId(commenterId)
                .type(Type.COMMENT)
                .isModalOpen(isModalOpen)
                .count(count)
                .build();
    }

    public NotificationResponse toReplyNotification(int commentId, int replierId) throws ResourceNotFoundException {
        final CommentDTO commentDTO = commentService.getById(commentId);
        final User replier = userService.getById(replierId);

        boolean isModalOpen = userService.isModalOpen(commentDTO.getCommenterId(), commentId, Type.REPLY);
        int count = replyService.getNotificationCountForRespondent(commentDTO.getCommenterId(), commentId, replierId);
        return NotificationResponse.builder()
                .id(commentId)
                .message(replier.getName() + " replied to your comment: " +  "\"" + commentDTO.getBody() + "\"")
                .respondentPicture(replier.getPicture())
                .respondentId(replierId)
                .type(Type.REPLY)
                .count(count)
                .isModalOpen(isModalOpen)
                .build();
    }

    public NotificationResponse toMentionNotification(Mention mention) throws ResourceNotFoundException {
        User mentioningUser = mention.getMentioningUser();
        String message = getMessage(mentioningUser, mention.getType(), mention.getTypeId());
        return NotificationResponse.builder()
                .id(mention.getId())
                .message(message)
                .respondentPicture(mentioningUser.getPicture())
                .formattedDate(Formatter.formatDate(mention.getCreatedAt()))
                .formattedTime(Formatter.formatTime(mention.getCreatedAt()))
                .build();
    }

    private String getMessage(User mentioningUser, Type type, int typeId) throws ResourceNotFoundException {
        return switch (type) {
            case POST -> mentioningUser.getName() + " mentioned you in his/her post: " + "\"" + postService.getById(typeId).getBody() + "\"";
            case COMMENT -> mentioningUser.getName() + " mentioned you in his/her comment: " + "\"" + commentService.getById(typeId).getBody() + "\"";
            case REPLY -> mentioningUser.getName() + " mentioned you in his/her reply: " + "\"" + replyService.getById(typeId).getBody() + "\"";
        };
    }
}
