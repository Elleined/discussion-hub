package com.forum.application.service;

import com.forum.application.dto.CommentDTO;
import com.forum.application.dto.PostDTO;
import com.forum.application.dto.ReplyDTO;
import com.forum.application.exception.*;
import com.forum.application.mapper.CommentMapper;
import com.forum.application.mapper.PostMapper;
import com.forum.application.mapper.ReplyMapper;
import com.forum.application.model.Comment;
import com.forum.application.model.Post;
import com.forum.application.model.Reply;
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
    private final LikeService likeService;
    private final NotificationService notificationService;

    private final PostMapper postMapper;
    private final CommentMapper commentMapper;
    private final ReplyMapper replyMapper;

    public PostDTO savePost(String body, Set<Integer> mentionedUserIds) throws EmptyBodyException,
            BlockedException,
            ResourceNotFoundException,
            NoLoggedInUserException {

        if (Validator.isValidBody(body)) throw new EmptyBodyException("Body cannot be empty! Please provide text for your post to be posted!");

        int currentUserId = userService.getCurrentUser().getId();
        int postId = postService.save(currentUserId, body);
        if (mentionedUserIds != null) {
            userService.mentionUsers(currentUserId, mentionedUserIds, Type.POST, postId)
                    .forEach(notificationService::broadcastMentionNotification);
        }
        return postService.getById(postId);
    }

    public CommentDTO saveComment(int postId, String body, String attachedPicture, Set<Integer> mentionedUserIds) throws ResourceNotFoundException,
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

        Comment comment = commentService.save(currentUserId, postId, body, attachedPicture);

        if (mentionedUserIds != null) {
            userService.mentionUsers(currentUserId, mentionedUserIds, Type.COMMENT, comment.getId())
                    .forEach(notificationService::broadcastMentionNotification);
        }

        wsService.broadcastComment(comment);
        notificationService.broadcastCommentNotification(comment);
        return commentMapper.toDTO(comment);
    }

    public ReplyDTO saveReply(int commentId, String body, String attachedPicture, Set<Integer> mentionedUserIds) throws EmptyBodyException,
            NoLoggedInUserException,
            ClosedCommentSectionException,
            ResourceNotFoundException,
            BlockedException {

        int currentUserId = userService.getCurrentUser().getId();
        int commenterId = commentService.getById(commentId).getCommenterId();

        if (Validator.isValidBody(body)) throw new EmptyBodyException("Reply body cannot be empty!");
        if (commentService.isCommentSectionClosed(commentId)) throw new ClosedCommentSectionException("Cannot reply to this comment because author already closed the comment section for this post!");
        if (commentService.isDeleted(commentId)) throw new ResourceNotFoundException("The comment you trying to reply is either be deleted or does not exists anymore!");
        if (userService.isYouBeenBlockedBy(currentUserId, commenterId)) throw new BlockedException("Cannot reply because this user block you already!");

        Reply reply = replyService.save(currentUserId, commentId, body, attachedPicture);
        if (mentionedUserIds != null){
            userService.mentionUsers(currentUserId, mentionedUserIds, Type.REPLY, reply.getId())
                    .forEach(notificationService::broadcastMentionNotification);
        }

        wsService.broadcastReply(reply);
        notificationService.broadcastReplyNotification(reply);
        return replyMapper.toDTO(reply);
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
        Comment comment = commentService.delete(commentId);
        wsService.broadcastComment(comment);
    }

    public void deleteReply(int replyId) {
        Reply reply = replyService.delete(replyId);
        wsService.broadcastReply(reply);
    }

    public List<PostDTO> getAllPost() {
        return postService.getAll();
    }

    public List<PostDTO> getAllByAuthorId(int authorId) {
        return postService.getAllByAuthorId(authorId);
    }

    public List<CommentDTO> getAllCommentsOf(int postId) {
        commentService.readAllComments(postId);
        userService.readAllCommentsMention(postId);
        return commentService.getAllCommentsOf(postId);
    }

    public List<ReplyDTO> getAllRepliesOf(int commentId) {
        replyService.readAllReplies(commentId);
        userService.readAllRepliesMention(commentId);
        return replyService.getAllRepliesOf(commentId);
    }

    public long getAllUnreadNotificationCount(int userId) {
        return notificationService.getAllUnreadNotificationCount(userId);
    }

    public CommentDTO updateUpvote(int commentId) throws NoLoggedInUserException,
            ResourceNotFoundException,
            UpvoteException {

        int currentUserId = userService.getCurrentUser().getId();
        if (commentService.isDeleted(commentId)) throw new ResourceNotFoundException("The comment you trying to upvote might be deleted by the author or does not exists anymore!");
        if (commentService.isUserAlreadyUpvoteComment(currentUserId, commentId)) throw new UpvoteException("You can only up vote and down vote a comment once!");

        return commentService.updateUpvote(currentUserId, commentId);
    }

    public PostDTO updateCommentSectionStatus(int postId, Post.CommentSectionStatus status) {
        postService.updateCommentSectionStatus(postId, status);
        return postService.getById(postId);
    }

    public PostDTO updatePostBody(int postId, String newBody) {
        postService.updatePostBody(postId, newBody);
        return postService.getById(postId);
    }

    public PostDTO likePost(int userId, int postId) {
        Post post = likeService.addPostLike(userId, postId);
        return postMapper.toDTO(post);
    }
    public CommentDTO updateCommentBody(int commentId, String newBody) {
        Comment comment = commentService.updateCommentBody(commentId, newBody);
        wsService.broadcastComment(comment);
        return commentMapper.toDTO(comment);
    }

    public ReplyDTO updateReplyBody(int replyId, String newReplyBody) {
        Reply reply = replyService.updateReplyBody(replyId, newReplyBody);
        wsService.broadcastReply(reply);

        return replyMapper.toDTO(reply);
    }

    public String getCommentSectionStatus(int postId) {
        return postService.getCommentSectionStatus(postId);
    }

}
