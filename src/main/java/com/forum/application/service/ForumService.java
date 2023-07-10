package com.forum.application.service;

import com.forum.application.dto.CommentDTO;
import com.forum.application.dto.PostDTO;
import com.forum.application.dto.ReplyDTO;
import com.forum.application.dto.notification.NotificationResponse;
import com.forum.application.exception.*;
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

    public PostDTO savePost(String body, Set<Integer> mentionedUserIds) throws EmptyBodyException,
            ResourceNotFoundException,
            NoLoggedInUserException {

        if (Validator.isValidBody(body)) throw new EmptyBodyException("Body cannot be empty! Please provide text for your post to be posted!");

        int currentUserId = userService.getCurrentUser().getId();
        int postId = postService.save(currentUserId, body);
        if (mentionedUserIds != null) this.mentionUsers(currentUserId, mentionedUserIds, Type.POST, postId);
        return postService.getById(postId);
    }

    public CommentDTO saveComment(int postId, String body, Set<Integer> mentionedUserIds) throws ResourceNotFoundException,
            NoLoggedInUserException,
            ClosedCommentSectionException,
            BlockedException,
            EmptyBodyException {

        int currentUserId = userService.getCurrentUser().getId();
        int authorId = postService.getById(postId).getAuthorId();

        if (Validator.isValidBody(body)) throw new EmptyBodyException("Comment body cannot be empty! Please provide text for your comment");
        if (postService.isCommentSectionClosed(postId)) throw new ClosedCommentSectionException("Cannot comment because author already closed the comment section for this post!");
        if (postService.isDeleted(postId)) throw new ResourceNotFoundException("The post you trying to comment is either be deleted or does not exists anymore!");
        if (userService.isYouBeenBlockedBy(currentUserId, authorId)) throw new BlockedException("Cannot comment because this user block you already!");

        int commentId = commentService.save(currentUserId, postId, body);
        wsService.broadcastComment(commentId);
        notificationService.broadcastCommentNotification(postId, currentUserId);

        if (mentionedUserIds != null) this.mentionUsers(currentUserId, mentionedUserIds, Type.COMMENT, commentId);
        return commentService.getById(commentId);
    }

    public ReplyDTO saveReply(int commentId, String body, Set<Integer> mentionedUserIds) throws EmptyBodyException,
            NoLoggedInUserException,
            ClosedCommentSectionException,
            ResourceNotFoundException {

        int currentUserId = userService.getCurrentUser().getId();
        int commenterId = commentService.getById(commentId).getCommenterId();

        if (Validator.isValidBody(body)) throw new EmptyBodyException("Reply body cannot be empty!");
        if (commentService.isCommentSectionClosed(commentId)) throw new ClosedCommentSectionException("Cannot reply to this comment because author already closed the comment section for this post!");
        if (commentService.isDeleted(commentId)) throw new ResourceNotFoundException("The comment you trying to reply is either be deleted or does not exists anymore!");
        if (userService.isYouBeenBlockedBy(currentUserId, commenterId)) throw new BlockedException("Cannot reply because this user block you already!");

        int replyId = replyService.save(currentUserId, commentId, body);
        wsService.broadcastReply(replyId);
        notificationService.broadcastReplyNotification(commentId, currentUserId);

        if (mentionedUserIds != null) this.mentionUsers(currentUserId, mentionedUserIds, Type.REPLY, replyId);
        return replyService.getById(replyId);
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

    public CommentDTO updateUpvote(int commentId, int newUpvoteCount) throws NoLoggedInUserException,
            ResourceNotFoundException,
            UpvoteException {

        int currentUserId = userService.getCurrentUser().getId();
        int oldUpvoteCount = commentService.getById(commentId).getUpvote();
        if (commentService.isDeleted(commentId)) throw new ResourceNotFoundException("The comment you trying to upvote might be deleted by the author or does not exists anymore!");
        if (commentService.isUserAlreadyUpvoteComment(currentUserId, commentId)) throw new UpvoteException("You can only up vote and down vote a comment once!");
        if (commentService.isNotValidUpvoteValue(oldUpvoteCount, newUpvoteCount)) throw new UpvoteException("Cannot update upvote count! Because new upvote count must only be + 1 or - 1 to the previous value!");

        return commentService.updateUpvote(currentUserId, commentId, newUpvoteCount);
    }

    public PostDTO updateCommentSectionStatus(int postId, Post.CommentSectionStatus status) {
        postService.updateCommentSectionStatus(postId, status);
        return postService.getById(postId);
    }

    public PostDTO updatePostBody(int postId, String newBody) {
        postService.updatePostBody(postId, newBody);
        return postService.getById(postId);
    }

    public CommentDTO updateCommentBody(int commentId, String newBody) {
        commentService.updateCommentBody(commentId, newBody);
        wsService.broadcastComment(commentId);

        return commentService.getById(commentId);
    }

    public ReplyDTO updateReplyBody(int replyId, String newReplyBody) {
        replyService.updateReplyBody(replyId, newReplyBody);
        wsService.broadcastReply(replyId);

        return replyService.getById(replyId);
    }

    public String getCommentSectionStatus(int postId) {
        return postService.getCommentSectionStatus(postId);
    }
}
