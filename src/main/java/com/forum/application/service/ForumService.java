package com.forum.application.service;

import com.forum.application.dto.CommentDTO;
import com.forum.application.dto.PostDTO;
import com.forum.application.dto.ReplyDTO;
import com.forum.application.dto.notification.NotificationResponse;
import com.forum.application.exception.EmptyBodyException;
import com.forum.application.exception.ResourceNotFoundException;
import com.forum.application.model.NotificationStatus;
import com.forum.application.model.Post;
import com.forum.application.model.Type;
import com.forum.application.validator.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class ForumService {
    private final UserService userService;
    private final PostService postService;
    private final CommentService commentService;
    private final ReplyService replyService;
    private final WSService wsService;
    private final NotificationService notificationService;
    private final MentionService mentionService;

    public int savePost(String body, Set<Integer> mentionedUserIds) throws EmptyBodyException, ResourceNotFoundException {
        if (Validator.isValidBody(body)) throw new EmptyBodyException("Body cannot be empty! Please provide text for your post to be posted!");

        int currentUserId = userService.getCurrentUser().getId();

        int postId = postService.save(currentUserId, body);
        if (mentionedUserIds != null) this.mentionUsers(currentUserId, mentionedUserIds, Type.POST, postId);
        return postId;
    }

    public int saveComment(int commenterId, int postId, String body) {
        int commentId = commentService.save(commenterId, postId, body);
        wsService.broadcastComment(commentId);
        notificationService.broadcastCommentNotification(postId, commenterId);

        return commentId;
    }

    public int saveReply(int replierId, int commentId, String body) {
        int replyId = replyService.save(replierId, commentId, body);
        wsService.broadcastReply(replyId);
        notificationService.broadcastReplyNotification(commentId, replierId);
        return replyId;
    }

    public void mentionUsers(int mentioningUserId, Set<Integer> usersToBeMentionIds, Type type, int typeId) {
        usersToBeMentionIds.stream()
                .map(usersToBeMentionId -> mentionService.save(mentioningUserId, usersToBeMentionId, type, typeId))
                .forEach(notificationService::broadcastMentionNotification);
    }

    public PostDTO getPostById(int postId) {
        return postService.getById(postId);
    }
    public CommentDTO getCommentById(int commentId) {
        return commentService.getById(commentId);
    }
    public ReplyDTO getReplyById(int replyId) {
        return replyService.getById(replyId);
    }

    public boolean isEmpty(String body) {
        return body == null || body.isEmpty() || body.isBlank();
    }

    public void deletePost(int postId) {
        postService.delete(postId);
    }

    public void deleteComment(int commentId) {
        commentService.delete(commentId);
        wsService.broadcastComment(commentId);
    }

    public void deleteReply(int replyId) {
        replyService.delete(replyId);
        wsService.broadcastReply(replyId);
    }

    public List<PostDTO> getAllPost() {
        return postService.getAll();
    }

    public List<PostDTO> getAllByAuthorId(int authorId) {
        return postService.getAllByAuthorId(authorId);
    }

    public List<CommentDTO> getAllCommentsOf(int postId) {
        commentService.updateAllCommentNotificationStatusByPostId(postId, NotificationStatus.READ);
        return commentService.getAllCommentsOf(postId);
    }

    public List<ReplyDTO> getAllRepliesOf(int commentId) {
        replyService.updateAllRepliesByCommentId(commentId, NotificationStatus.READ);
        return replyService.getAllRepliesOf(commentId);
    }

    public long getAllUnreadNotificationCount(int userId) {
        return notificationService.getAllUnreadNotificationCount(userId);
    }

    public Set<NotificationResponse> getAllNotification(int userId) {
        return notificationService.getAllNotification(userId);
    }

    public CommentDTO updateUpvote(int respondentId, int commentId, int newUpvoteCount) {
        return commentService.updateUpvote(respondentId, commentId, newUpvoteCount);
    }

    public void updateCommentSectionStatus(int postId, Post.CommentSectionStatus status) {
        postService.updateCommentSectionStatus(postId, status);
    }

    public void updatePostBody(int postId, String newBody) {
        postService.updatePostBody(postId, newBody);
    }

    public void updateCommentBody(int commentId, String newBody) {
        commentService.updateCommentBody(commentId, newBody);
        wsService.broadcastComment(commentId);
    }

    public void updateReplyBody(int replyId, String newReplyBody) {
        replyService.updateReplyBody(replyId, newReplyBody);
        wsService.broadcastReply(replyId);
    }

    public String getCommentSectionStatus(int postId) {
        return postService.getCommentSectionStatus(postId);
    }

    public boolean isNotValidUpvoteValue(int commentId, int newUpvoteValue) {
        CommentDTO commentDTO = commentService.getById(commentId);
        return commentService.isNotValidUpvoteValue(commentDTO.getUpvote(), newUpvoteValue);
    }

    public boolean isUserAlreadyUpvoteComment(int respondentId, int commentId) {
        return commentService.isUserAlreadyUpvoteComment(respondentId, commentId);
    }

    public boolean isPostDeleted(int postId) {
        return postService.isDeleted(postId);
    }

    public boolean isCommentDeleted(int commentId) {
        return commentService.isDeleted(commentId);
    }

    public boolean isPostCommentSectionClosed(int postId) {
        return postService.getCommentSectionStatus(postId).equals(Post.CommentSectionStatus.CLOSED.name());
    }

    public boolean isPostCommentSectionClosedByCommentId(int commentId) {
        int postId = commentService.getById(commentId).getPostId();
        return this.isPostCommentSectionClosed(postId);
    }
}
