package com.forum.application.mapper;

import com.forum.application.dto.CommentDTO;
import com.forum.application.dto.MentionResponse;
import com.forum.application.dto.PostDTO;
import com.forum.application.dto.notification.CommentNotificationResponse;
import com.forum.application.dto.notification.NotificationResponse;
import com.forum.application.dto.notification.ReplyNotificationResponse;
import com.forum.application.exception.ResourceNotFoundException;
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
    private final MentionService mentionService;
    private final MentionMapper mentionMapper;

    @Autowired @Lazy
    public NotificationMapper(PostService postService, UserService userService, CommentService commentService, ReplyService replyService, MentionService mentionService, MentionMapper mentionMapper) {
        this.postService = postService;
        this.userService = userService;
        this.commentService = commentService;
        this.replyService = replyService;
        this.mentionService = mentionService;
        this.mentionMapper = mentionMapper;
    }

    public NotificationResponse toCommentNotification(int postId, int commenterId) throws ResourceNotFoundException {
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

    public NotificationResponse toReplyNotification(int commentId, int replierId) throws ResourceNotFoundException {
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

    public MentionResponse toMentionNotification(int mentionId) {
        MentionResponse mentionResponse = mentionMapper.toDTO(mentionService.getById(mentionId));
        User mentioningUser = userService.getById(mentionResponse.getMentioningUserId());
        String message = switch (Type.valueOf(mentionResponse.getType())) {
            case POST -> mentioningUser.getName() + " mention you in a post: " + "\"" + postService.getById(mentionResponse.getTypeId()).getBody() + "\"";
            case COMMENT -> mentioningUser.getName() + " mention you in a comment " + "\"" + commentService.getById(mentionResponse.getTypeId()).getBody() + "\"";
            case REPLY -> mentioningUser.getName() + " mention you in a reply " + "\"" + replyService.getById(mentionResponse.getTypeId()).getBody() + "\"";
        };
        mentionResponse.setMessage(message);
        return mentionResponse;
    }
}
