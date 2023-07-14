package com.forum.application.mapper;

import com.forum.application.dto.*;
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
    private final MentionHelper mentionHelper;

    @Autowired @Lazy
    public NotificationMapper(PostService postService, UserService userService, CommentService commentService, ReplyService replyService, MentionHelper mentionHelper) {
        this.postService = postService;
        this.userService = userService;
        this.commentService = commentService;
        this.replyService = replyService;
        this.mentionHelper = mentionHelper;
    }

    public NotificationResponse toCommentNotification(int commentId, int postId) throws ResourceNotFoundException {
        final PostDTO postDTO = postService.getById(postId);
        final CommentDTO commentDTO = commentService.getById(commentId);

        boolean isModalOpen = userService.isModalOpen(postDTO.getAuthorId(), postId, Type.COMMENT);
        int count = commentService.getNotificationCountForRespondent(postDTO.getAuthorId(), postId, commentDTO.getCommenterId());
        return NotificationResponse.builder()
                .id(postId)
                .message(commentDTO.getCommenterName() + " commented in your post: " + "\"" + postDTO.getBody() + "\"")
                .respondentPicture(commentDTO.getCommenterPicture())
                .respondentId(commentDTO.getCommenterId())
                .type(Type.COMMENT)
                .isModalOpen(isModalOpen)
                .count(count)
                .formattedTime(commentDTO.getFormattedTime())
                .formattedDate(commentDTO.getFormattedDate())
                .build();
    }

    public NotificationResponse toReplyNotification(int replyId, int commentId) throws ResourceNotFoundException {
        final CommentDTO commentDTO = commentService.getById(commentId);
        final ReplyDTO replyDTO = replyService.getById(replyId);

        boolean isModalOpen = userService.isModalOpen(commentDTO.getCommenterId(), commentId, Type.REPLY);
        int count = replyService.getNotificationCountForRespondent(commentDTO.getCommenterId(), commentId, replyDTO.getReplierId());
        return ReplyNotification.replyNotificationBuilder()
                .id(commentId)
                .message(replyDTO.getReplierName() + " replied to your comment: " +  "\"" + commentDTO.getBody() + "\"")
                .respondentPicture(replyDTO.getReplierPicture())
                .respondentId(replyDTO.getReplierId())
                .type(Type.REPLY)
                .count(count)
                .isModalOpen(isModalOpen)
                .formattedDate(replyDTO.getFormattedDate())
                .formattedTime(replyDTO.getFormattedTime())
                .postId(commentDTO.getPostId())
                .build();
    }

    public NotificationResponse toMentionNotification(Mention mention) throws ResourceNotFoundException {
        User mentioningUser = mention.getMentioningUser();
        User mentionedUser = mention.getMentionedUser();
        String message = mentionHelper.getMessage(mentioningUser, mention.getType(), mention.getTypeId());

        int parentId = mentionHelper.getParentId(mention.getType(), mention.getTypeId());
        boolean isModalOpen = userService.isModalOpen(mentionedUser.getId(), parentId, mention.getType());
        return NotificationResponse.builder()
                .id(mention.getId())
                .message(message)
                .respondentPicture(mentioningUser.getPicture())
                .formattedDate(Formatter.formatDate(mention.getCreatedAt()))
                .formattedTime(Formatter.formatTime(mention.getCreatedAt()))
                .isModalOpen(isModalOpen)
                .build();
    }
}
