package com.forum.application.service;

import com.forum.application.dto.CommentDTO;
import com.forum.application.exception.NoLoggedInUserException;
import com.forum.application.exception.ResourceNotFoundException;
import com.forum.application.mapper.CommentMapper;
import com.forum.application.model.*;
import com.forum.application.repository.CommentRepository;
import com.forum.application.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class CommentService {
    private final UserService userService;
    private final PostRepository postRepository;
    private final ReplyService replyService;
    private final BlockService blockService;
    private final ModalTrackerService modalTrackerService;
    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;

    Comment save(int currentUserId, int postId, String body, String attachedPicture) throws ResourceNotFoundException {
        User commenter = userService.getById(currentUserId);
        Post post = postRepository.findById(postId).orElseThrow(() -> new ResourceNotFoundException("Post with id of " + postId + " does not exists!"));

        NotificationStatus status = modalTrackerService.isModalOpen(post.getAuthor().getId(), postId, ModalTracker.Type.COMMENT)
                ? NotificationStatus.READ
                : NotificationStatus.UNREAD;

        Comment comment = Comment.builder()
                .body(body)
                .dateCreated(LocalDateTime.now())
                .post(post)
                .commenter(commenter)
                .attachedPicture(attachedPicture)
                .notificationStatus(status)
                .status(Status.ACTIVE)
                .replies(new ArrayList<>())
                .likes(new HashSet<>())
                .mentions(new HashSet<>())
                .build();

        commenter.getComments().add(comment);
        post.getComments().add(comment);
        commentRepository.save(comment);
        log.debug("Comment with id of {} saved successfully", comment.getId());
        return comment;
    }

    Comment delete(int commentId) {
        Comment comment = getById(commentId);
        log.debug("Comment with id of {} are now inactive!", commentId);
        return this.setStatus(comment);
    }

    public boolean isDeleted(int commentId) throws ResourceNotFoundException {
        Comment comment = getById(commentId);
        return comment.getStatus() == Status.INACTIVE;
    }

    public boolean isDeleted(Comment comment) throws ResourceNotFoundException {
        return comment.getStatus() == Status.INACTIVE;
    }

    List<CommentDTO> getAllCommentsOf(int postId) throws ResourceNotFoundException, NoLoggedInUserException {
        int currentUserId = userService.getCurrentUser().getId();

        Post post = postRepository.findById(postId).orElseThrow(() -> new ResourceNotFoundException("Post with id of " + postId + " does not exists!"));
        return post.getComments()
                .stream()
                .filter(comment -> comment.getStatus() == Status.ACTIVE)
                .filter(comment -> !blockService.isBlockedBy(currentUserId, comment.getCommenter().getId()))
                .filter(comment -> !blockService.isYouBeenBlockedBy(currentUserId, comment.getCommenter().getId()))
                .sorted(Comparator.comparingInt(Comment::getUpvote).reversed())
                .map(commentMapper::toDTO)
                .toList();
    }

    public Comment getById(int commentId) throws ResourceNotFoundException {
        return commentRepository.findById(commentId).orElseThrow(() -> new ResourceNotFoundException("Comment with id of " + commentId + " does not exists!"));
    }

    public List<CommentDTO> getAllUnreadComments(User currentUser, int postId) throws ResourceNotFoundException {
        Post post = currentUser.getPosts().stream()
                .filter(userPost -> userPost.getId() == postId)
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Author with id of " + currentUser.getId() + " does not have post with id of " + postId));

        return post.getComments()
                .stream()
                .filter(comment -> comment.getStatus() == Status.ACTIVE)
                .filter(comment -> comment.getNotificationStatus() == NotificationStatus.UNREAD)
                .filter(comment -> !blockService.isBlockedBy(currentUser.getId(), comment.getCommenter().getId()))
                .filter(comment -> !blockService.isYouBeenBlockedBy(currentUser.getId(), comment.getCommenter().getId()))
                .map(commentMapper::toDTO)
                .toList();
    }

    public int getNotificationCountForRespondent(User currentUser, int postId, int respondentId) throws ResourceNotFoundException {
        return (int) getAllUnreadComments(currentUser, postId)
                .stream()
                .filter(comment -> comment.getCommenterId() == respondentId)
                .count();
    }

    public int getNotificationCountForSpecificPost(User currentUser, int postId) throws ResourceNotFoundException {
        return getAllUnreadComments(currentUser, postId).size();
    }

    public Set<Comment> getUnreadCommentsOfAllPost(User currentUser) throws ResourceNotFoundException {
        List<Post> posts = currentUser.getPosts();

        return posts.stream()
                .map(Post::getComments)
                .flatMap(comments -> comments.stream()
                        .filter(comment -> comment.getStatus() == Status.ACTIVE)
                        .filter(comment -> comment.getNotificationStatus() == NotificationStatus.UNREAD)
                        .filter(comment -> !blockService.isBlockedBy(currentUser.getId(), comment.getCommenter().getId()))
                        .filter(comment -> !blockService.isYouBeenBlockedBy(currentUser.getId(), comment.getCommenter().getId())))
                .collect(Collectors.toSet());
    }

    int updateUpvote(int respondentId, int commentId) throws ResourceNotFoundException {
        this.setUpvote(respondentId, commentId);
        log.debug("User with id of {} upvoted the Comment with id of {} successfully", respondentId, commentId);
        return commentId;
    }

    Comment updateCommentBody(int commentId, String newBody) throws ResourceNotFoundException {
        Comment comment = getById(commentId);
        if (comment.getBody().equals(newBody)) return comment;
        comment.setBody(newBody);
        commentRepository.save(comment);
        log.debug("Comment with id of {} updated with the new body of {}", commentId, newBody);
        return comment;
    }

    private void readComment(int commentId) throws ResourceNotFoundException {
        Comment comment = getById(commentId);
        comment.setNotificationStatus(NotificationStatus.READ);
        commentRepository.save(comment);
        log.debug("Comment with id of {} notification status updated to {}", commentId, NotificationStatus.READ);
    }

    public void readAllComments(int postId) throws ResourceNotFoundException, NoLoggedInUserException {
        int currentUserId = userService.getCurrentUser().getId();

        Post post = postRepository.findById(postId).orElseThrow(() -> new ResourceNotFoundException("Post with id of " + postId + " does not exists!"));
        if (currentUserId != post.getAuthor().getId()) {
            log.trace("Will not mark as unread because the current user with id of {} are not the author of the post who is {}", currentUserId, post.getAuthor().getId());
            return;
        }
        log.trace("Will mark all as read becuase the current user with id of {} is the author of the post {}", currentUserId, post.getAuthor().getId());
        post.getComments()
                .stream()
                .filter(comment -> comment.getStatus() == Status.ACTIVE)
                .filter(comment -> !blockService.isBlockedBy(currentUserId, comment.getCommenter().getId()))
                .filter(comment -> !blockService.isYouBeenBlockedBy(currentUserId, comment.getCommenter().getId()))
                .map(Comment::getId)
                .forEach(this::readComment);
    }


    boolean isUserAlreadyUpvoteComment(int respondentId, int commentId) throws ResourceNotFoundException {
        User respondent = userService.getById(respondentId);
        return respondent.getUpvotedComments()
                .stream()
                .anyMatch(upvotedComment -> upvotedComment.getId() == commentId);
    }

    boolean isCommentSectionClosed(int commentId) throws ResourceNotFoundException {
        Comment comment = getById(commentId);
        Post post = comment.getPost();
        return post.getCommentSectionStatus() == Post.CommentSectionStatus.CLOSED;
    }

    Comment setStatus(Comment comment) throws ResourceNotFoundException {
        comment.setStatus(Status.INACTIVE);
        commentRepository.save(comment);

        comment.getReplies().forEach(replyService::setStatus);
        return comment;
    }

    private void setUpvote(int respondentId, int commentId) throws ResourceNotFoundException {
        Comment comment = getById(commentId);
        comment.setUpvote(comment.getUpvote() + 1);
        commentRepository.save(comment);

        User respondent = userService.getById(respondentId);
        respondent.getUpvotedComments().add(comment);
        userService.save(respondent);
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public int getTotalReplies(Comment comment) {
        return (int) comment.getReplies().stream()
                .filter(reply -> reply.getStatus() == Status.ACTIVE)
                .count();
    }
}
