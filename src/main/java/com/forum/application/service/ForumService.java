package com.forum.application.service;

import com.forum.application.dto.*;
import com.forum.application.exception.*;
import com.forum.application.mapper.CommentMapper;
import com.forum.application.mapper.PostMapper;
import com.forum.application.mapper.ReplyMapper;
import com.forum.application.mapper.UserMapper;
import com.forum.application.model.*;
import com.forum.application.model.like.Like;
import com.forum.application.model.mention.Mention;
import com.forum.application.validator.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class ForumService {
    private final UserService userService;
    private final PostService postService;
    private final CommentService commentService;
    private final ReplyService replyService;
    private final BlockService blockService;
    private final WSService wsService;
    private final LikeService likeService;
    private final ModalTrackerService modalTrackerService;
    private final WSNotificationService WSNotificationService;
    private final MentionService mentionService;
    private final NotificationService notificationService;

    private final PostMapper postMapper;
    private final CommentMapper commentMapper;
    private final ReplyMapper replyMapper;
    private final UserMapper userMapper;

    public PostDTO savePost(String body, String attachedPicture, Set<Integer> mentionedUserIds) throws EmptyBodyException,
            BlockedException,
            ResourceNotFoundException,
            NoLoggedInUserException {

        if (Validator.isValidBody(body)) throw new EmptyBodyException("Body cannot be empty! Please provide text for your post to be posted!");

        User currentUser = userService.getCurrentUser();
        Post post = postService.save(currentUser.getId(), body, attachedPicture);

        if (mentionedUserIds != null) {
            Set<Mention> mentions = mentionService.addAllMention(currentUser, mentionedUserIds, post);
            WSNotificationService.broadcastMentions(mentions);
            // broadcast mention notification here
        }
        return postMapper.toDTO(post);
    }

    public CommentDTO saveComment(int postId, String body, String attachedPicture, Set<Integer> mentionedUserIds) throws ResourceNotFoundException,
            NoLoggedInUserException,
            ClosedCommentSectionException,
            BlockedException,
            EmptyBodyException {

        User currentUser = userService.getCurrentUser();
        int authorId = postService.getById(postId).getAuthor().getId();

        if (Validator.isValidBody(body)) throw new EmptyBodyException("Comment body cannot be empty! Please provide text for your comment");
        if (postService.isCommentSectionClosed(postId)) throw new ClosedCommentSectionException("Cannot comment because author already closed the comment section for this post!");
        if (postService.isDeleted(postId)) throw new ResourceNotFoundException("The post you trying to comment is either be deleted or does not exists anymore!");
        if (blockService.isYouBeenBlockedBy(currentUser.getId(), authorId)) throw new BlockedException("Cannot comment because this user block you already!");

        Comment comment = commentService.save(currentUser.getId(), postId, body, attachedPicture);

        if (mentionedUserIds != null) {
            Set<Mention> mentions = mentionService.addAllMention(currentUser, mentionedUserIds, comment);
            WSNotificationService.broadcastMentions(mentions);
            // broadcast comment mention here
        }

        wsService.broadcastComment(comment);
        WSNotificationService.broadcastCommentNotification(comment);
        return commentMapper.toDTO(comment);
    }

    public ReplyDTO saveReply(int commentId, String body, String attachedPicture, Set<Integer> mentionedUserIds) throws EmptyBodyException,
            NoLoggedInUserException,
            ClosedCommentSectionException,
            ResourceNotFoundException,
            BlockedException {

        User currentUser = userService.getCurrentUser();
        int commenterId = commentService.getById(commentId).getCommenter().getId();

        if (Validator.isValidBody(body)) throw new EmptyBodyException("Reply body cannot be empty!");
        if (commentService.isCommentSectionClosed(commentId)) throw new ClosedCommentSectionException("Cannot reply to this comment because author already closed the comment section for this post!");
        if (commentService.isDeleted(commentId)) throw new ResourceNotFoundException("The comment you trying to reply is either be deleted or does not exists anymore!");
        if (blockService.isYouBeenBlockedBy(currentUser.getId(), commenterId)) throw new BlockedException("Cannot reply because this user block you already!");

        Reply reply = replyService.save(currentUser.getId(), commentId, body, attachedPicture);

        if (mentionedUserIds != null) {
            Set<Mention> mentions = mentionService.addAllMention(currentUser, mentionedUserIds, reply);
            WSNotificationService.broadcastMentions(mentions);
        }

        wsService.broadcastReply(reply);
        WSNotificationService.broadcastReplyNotification(reply);
        return replyMapper.toDTO(reply);
    }

    public PostDTO getPostById(int postId) {
        Post post = postService.getById(postId);
        return postMapper.toDTO(post);
    }
    public CommentDTO getCommentById(int commentId) {
        Comment comment = commentService.getById(commentId);
        return commentMapper.toDTO(comment);
    }

    public ReplyDTO getReplyById(int replyId) {
        Reply reply = replyService.getById(replyId);
        return replyMapper.toDTO(reply);
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

    public List<PostDTO> getAllByAuthorId(int authorId) {
        return postService.getAllByAuthorId(authorId)
                .stream()
                .map(postMapper::toDTO)
                .toList();
    }

    public List<PostDTO> getAllPost() {
        likeService.readPostLikes(userService.getCurrentUser());
        mentionService.readPostMentions(userService.getCurrentUser());
        return postService.getAll()
                .stream()
                .map(postMapper::toDTO)
                .toList();
    }

    public List<CommentDTO> getAllCommentsOf(int postId) {
        commentService.readAllComments(postId);
        likeService.readCommentLikes(userService.getCurrentUser());
        mentionService.readCommentMentions(userService.getCurrentUser());
        return commentService.getAllCommentsOf(postId);
    }

    public List<ReplyDTO> getAllRepliesOf(int commentId) {
        replyService.readAllReplies(commentId);
        likeService.readReplyLikes(userService.getCurrentUser());
        mentionService.readReplyMentions(userService.getCurrentUser());
        return replyService.getAllRepliesOf(commentId);
    }

    public long getTotalNotificationCount(User currentUser) throws ResourceNotFoundException {
        return notificationService.getTotalNotificationCount(currentUser);
    }

    public Set<NotificationResponse> getAllNotification(User currentUser) throws ResourceNotFoundException {
        return notificationService.getAllNotification(currentUser);
    }

    public CommentDTO updateUpvote(int commentId) throws NoLoggedInUserException,
            ResourceNotFoundException,
            UpvoteException {

        int currentUserId = userService.getCurrentUser().getId();
        if (commentService.isDeleted(commentId)) throw new ResourceNotFoundException("The comment you trying to upvote might be deleted by the author or does not exists anymore!");
        if (commentService.isUserAlreadyUpvoteComment(currentUserId, commentId)) throw new UpvoteException("You can only up vote and down vote a comment once!");

        int updatedCommentId = commentService.updateUpvote(currentUserId, commentId);
        Comment comment = commentService.getById(updatedCommentId);
        return commentMapper.toDTO(comment);
    }

    public PostDTO updateCommentSectionStatus(int postId, Post.CommentSectionStatus status) {
        Post post = postService.updateCommentSectionStatus(postId, status);
        return postMapper.toDTO(post);
    }

    public PostDTO updatePostBody(int postId, String newBody) {
        Post post = postService.updatePostBody(postId, newBody);
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

    public List<UserDTO> getAllUser(int currentUserId) {
        return userService.getAllUser()
                .stream()
                .filter(user -> user.getId() != currentUserId)
                .filter(user -> !blockService.isBlockedBy(currentUserId, user.getId()))
                .filter(user -> !blockService.isYouBeenBlockedBy(currentUserId, user.getId()))
                .map(userMapper::toDTO)
                .toList();
    }

    public List<UserDTO> getSuggestedMentions(int currentUserId, String name) {
        return userService.getSuggestedMentions(name)
                .stream()
                .filter(user -> user.getId() != currentUserId)
                .filter(user -> !blockService.isBlockedBy(currentUserId, user.getId()))
                .filter(user -> !blockService.isYouBeenBlockedBy(currentUserId, user.getId()))
                .map(userMapper::toDTO)
                .toList();
    }

    public void blockUser(int userId, int userToBeBlockedId) {
        blockService.blockUser(userId, userToBeBlockedId);
    }

    public void unBlockUser(int userId, int userToBeUnblockedId) {
        blockService.unBlockUser(userId, userToBeUnblockedId);
    }

    public boolean isBlockedBy(int userId, int userToCheckId) {
        return blockService.isBlockedBy(userId, userToCheckId);
    }

    public boolean isYouBeenBlockedBy(int userId, int suspectedUserId) {
        return blockService.isYouBeenBlockedBy(userId, suspectedUserId);
    }

    public Set<UserDTO> getAllBlockedUsers(int currentUserId) {
        return blockService.getAllBlockedUsers(currentUserId)
                .stream()
                .map(userMapper::toDTO)
                .collect(Collectors.toSet());
    }

    public ModalTracker saveTrackerOfUserById(int receiverId, int associateTypeIdOpened, String type) {
        return modalTrackerService.saveTrackerOfUserById(receiverId, associateTypeIdOpened, type);
    }

    public ModalTracker getTrackerOfUserById(int userId) {
        return modalTrackerService.getTrackerOfUserById(userId);
    }

    public void deleteTrackerOfUserById(int userId, String type) {
        modalTrackerService.deleteTrackerOfUserById(userId, ModalTracker.Type.valueOf(type));
    }

    public PostDTO likePost(int respondentId, int postId) throws ResourceNotFoundException, BlockedException {
        Post post = postService.getById(postId);
        Optional<Like> like = likeService.like(post, respondentId);
        if (like.isEmpty()) return postMapper.toDTO(post);
        WSNotificationService.broadcastLike(like.orElseThrow());
        return postMapper.toDTO(post);
    }

    public CommentDTO likeComment(int respondentId, int commentId) throws ResourceNotFoundException, BlockedException {
        Comment comment = commentService.getById(commentId);
        Optional<Like> like = likeService.like(comment, respondentId);
        if (like.isEmpty()) return commentMapper.toDTO(comment);
        WSNotificationService.broadcastLike(like.orElseThrow());
        return commentMapper.toDTO(comment);
    }

    public ReplyDTO likeReply(int respondentId, int replyId) throws ResourceNotFoundException, BlockedException {
        Reply reply = replyService.getById(replyId);
        Optional<Like> like = likeService.like(reply, respondentId);
        if (like.isEmpty()) return replyMapper.toDTO(reply);
        WSNotificationService.broadcastLike(like.orElseThrow());
        return replyMapper.toDTO(reply);
    }
}
