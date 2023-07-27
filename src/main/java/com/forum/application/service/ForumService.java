package com.forum.application.service;

import com.forum.application.dto.*;
import com.forum.application.exception.*;
import com.forum.application.mapper.*;
import com.forum.application.model.*;
import com.forum.application.validator.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
            addAllPostMention(currentUser, mentionedUserIds, post);
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
            addAllCommentMention(currentUser, mentionedUserIds, comment);
            // broadcast comment mention here
        }

        wsService.broadcastComment(comment);
        WSNotificationService.broadcastCommentNotification(comment, currentUser);
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
            addAllReplyMention(currentUser, mentionedUserIds, reply);
            // broadcast reply mention here
        }

        wsService.broadcastReply(reply);
        WSNotificationService.broadcastReplyNotification(reply, currentUser);
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
        return postService.getAll()
                .stream()
                .map(postMapper::toDTO)
                .toList();
    }

    public List<CommentDTO> getAllCommentsOf(int postId) {
        commentService.readAllComments(postId);
        likeService.readCommentLikes(userService.getCurrentUser());
        return commentService.getAllCommentsOf(postId);
    }

    public List<ReplyDTO> getAllRepliesOf(int commentId) {
        replyService.readAllReplies(commentId);
        likeService.readReplyLikes(userService.getCurrentUser());
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
        if (postService.isDeleted(postId)) throw new ResourceNotFoundException("Cannot like/unlike! The post with id of " + postId + " you are trying to like/unlike might already been deleted or does not exists!");
        if (blockService.isBlockedBy(respondentId, post.getAuthor().getId())) throw new BlockedException("Cannot like/unlike! You blocked the author of this post with id of !" + post.getAuthor().getId());
        if (blockService.isYouBeenBlockedBy(respondentId, post.getAuthor().getId())) throw  new BlockedException("Cannot like/unlike! The author of this post with id of " + post.getAuthor().getId() + " already blocked you");

        if (likeService.isUserAlreadyLikedPost(respondentId, post)) {
            likeService.unlikePost(respondentId, post);
            return postMapper.toDTO(post);
        }
        likeService.likePost(respondentId, post);
        return postMapper.toDTO(post);
    }

    public CommentDTO likeComment(int respondentId, int commentId) throws ResourceNotFoundException, BlockedException {
        Comment comment = commentService.getById(commentId);
        if (commentService.isDeleted(commentId)) throw new ResourceNotFoundException("Cannot like/unlike! The comment with id of " + commentId + " you are trying to like/unlike might already been deleted or does not exists!");
        if (blockService.isBlockedBy(respondentId, comment.getCommenter().getId())) throw new BlockedException("Cannot like/unlike! You blocked the author of this comment with id of !" + comment.getCommenter().getId());
        if (blockService.isYouBeenBlockedBy(respondentId, comment.getCommenter().getId())) throw  new BlockedException("Cannot like/unlike! The author of this comment with id of " + comment.getCommenter().getId() + " already blocked you");

        if (likeService.isUserAlreadyLikedComment(respondentId, comment)) {
            likeService.unlikeComment(respondentId, comment);
            return commentMapper.toDTO(comment);
        }

        likeService.likeComment(respondentId, comment);
        return commentMapper.toDTO(comment);
    }

    public ReplyDTO likeReply(int respondentId, int replyId) throws ResourceNotFoundException, BlockedException {
        Reply reply = replyService.getById(replyId);
        if (replyService.isDeleted(replyId)) throw new ResourceNotFoundException("Cannot like/unlike! The reply with id of " + replyId + " you are trying to like/unlike might already be deleted or does not exists!");
        if (blockService.isBlockedBy(respondentId, reply.getReplier().getId())) throw new BlockedException("Cannot like/unlike! You blocked the author of this reply with id of !" + reply.getReplier().getId());
        if (blockService.isYouBeenBlockedBy(respondentId, reply.getReplier().getId())) throw  new BlockedException("Cannot like/unlike! The author of this reply with id of " + reply.getReplier().getId() + " already blocked you");

        if (likeService.isUserAlreadyLikeReply(respondentId, reply)) {
            likeService.unlikeReply(respondentId, reply);
            return replyMapper.toDTO(reply);
        }
        likeService.likeReply(respondentId, reply);
        return replyMapper.toDTO(reply);
    }

    public PostDTO addPostMention(User currentUser, int mentionedUserId, Post post) throws ResourceNotFoundException, BlockedException {
        if (postService.isDeleted(post)) throw new ResourceNotFoundException("Cannot mention! The post with id of " + post.getId() + " you are trying to mention might already been deleted or does not exists!");
        if (blockService.isBlockedBy(currentUser.getId(), mentionedUserId)) throw new BlockedException("Cannot mention! You blocked the mentioned user with id of !" + mentionedUserId);
        if (blockService.isYouBeenBlockedBy(currentUser.getId(), mentionedUserId)) throw  new BlockedException("Cannot mention! Mentioned user with id of " + mentionedUserId + " already blocked you");
        if (currentUser.getId() == mentionedUserId) throw new MentionException("Cannot mention! You are trying to mention yourself which is not possible!");
        mentionService.addPostMention(currentUser, mentionedUserId, post);
        return postMapper.toDTO(post);
    }

    public List<PostDTO> addAllPostMention(User currentUser, Set<Integer> mentionedUserIds, Post post) {
        return mentionedUserIds.stream()
                .map(mentionedUserId -> addPostMention(currentUser, mentionedUserId, post))
                .toList();
    }

    public CommentDTO addCommentMention(User currentUser, int mentionedUserId, Comment comment) throws ResourceNotFoundException, BlockedException {
        if (commentService.isDeleted(comment)) throw new ResourceNotFoundException("Cannot mention! The comment with id of " + comment.getId() + " you are trying to mention might already been deleted or does not exists!");
        if (blockService.isBlockedBy(currentUser.getId(), mentionedUserId)) throw new BlockedException("Cannot mention! You blocked the mentioned user with id of !" + mentionedUserId);
        if (blockService.isYouBeenBlockedBy(currentUser.getId(), mentionedUserId)) throw  new BlockedException("Cannot mention! Mentioned user with id of " + mentionedUserId + " already blocked you");
        if (currentUser.getId() == mentionedUserId) throw new MentionException("Cannot mention! You are trying to mention yourself which is not possible!");

        mentionService.addCommentMention(currentUser, mentionedUserId, comment);
        return commentMapper.toDTO(comment);
    }

    public List<CommentDTO> addAllCommentMention(User currentUser, Set<Integer> mentionedUserIds, Comment comment) {
        return mentionedUserIds.stream()
                .map(mentionedUserId -> addCommentMention(currentUser, mentionedUserId, comment))
                .toList();
    }

    public ReplyDTO addReplyMention(User currentUser, int mentionedUserId, Reply reply) throws ResourceNotFoundException, BlockedException {
        if (replyService.isDeleted(reply)) throw new ResourceNotFoundException("Cannot mention! The reply with id of " + reply.getId() + " you are trying to mention might already be deleted or does not exists!");
        if (blockService.isBlockedBy(currentUser.getId(), mentionedUserId)) throw new BlockedException("Cannot mention! You blocked the mentioned user with id of !" + mentionedUserId);
        if (blockService.isYouBeenBlockedBy(currentUser.getId(), mentionedUserId)) throw new BlockedException("Cannot mention! Mentioned userwith id of " + mentionedUserId + " already blocked you");
        if (currentUser.getId() == mentionedUserId) throw new MentionException("Cannot mention! You are trying to mention yourself which is not possible!");

        mentionService.addReplyMention(currentUser, mentionedUserId, reply);
        return replyMapper.toDTO(reply);
    }

    List<ReplyDTO> addAllReplyMention(User currentUser, Set<Integer> mentionedUserIds, Reply reply) {
        return mentionedUserIds.stream()
                .map(mentionedUserId -> addReplyMention(currentUser, mentionedUserId, reply))
                .toList();
    }
}
