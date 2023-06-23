package com.forum.application.service;

import com.forum.application.dto.CommentDTO;
import com.forum.application.dto.PostDTO;
import com.forum.application.dto.ReplyDTO;
import com.forum.application.model.NotificationStatus;
import com.forum.application.model.Post;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class ForumService {

    private final PostService postService;
    private final CommentService commentService;
    private final ReplyService replyService;
    private final WSService wsService;
    private final NotificationService notificationService;

    public int savePost(int authorId, String body) {
        return postService.save(authorId, body);
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

    public List<PostDTO> getAllPost(int userId) {
        return postService.getAll(userId);
    }

    public List<PostDTO> getAllByAuthorId(int authorId) {
        return postService.getAllByAuthorId(authorId);
    }

    public List<CommentDTO> getAllCommentsOf(int userId, int postId) {
        return commentService.getAllCommentsOf(userId, postId);
    }

    public List<ReplyDTO> getAllRepliesOf(int userId, int commentId) {
        return replyService.getAllRepliesOf(userId, commentId);
    }

    public List<CommentDTO> getAllCommentById(List<Integer> commentIds) {
        return commentService.getAllById(commentIds);
    }

    public List<ReplyDTO> getAllRepliesById(List<Integer> replyIds) {
        return replyService.getAllById(replyIds);
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

    public void updateCommentNotificationStatus(int commentId, NotificationStatus newStatus) {
        commentService.updateNotificationStatus(commentId, newStatus);
    }

    public void updateAllCommentNotificationStatus(List<Integer> commentIds, NotificationStatus newStatus) {
        commentService.batchUpdateNotificationStatus(commentIds, newStatus);
    }

    public void updateReplyNotificationStatus(int replyId, NotificationStatus newStatus) {
        replyService.updateNotificationStatus(replyId, newStatus);
    }

    public void updateAllReplyNotificationStatus(List<Integer> replyIds, NotificationStatus newStatus) {
        replyService.batchUpdateNotificationStatus(replyIds, newStatus);
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
