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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class CommentService {
    private final UserService userService;
    private final PostRepository postRepository;
    private final ReplyService replyService;
    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;

    Comment save(int currentUserId, int postId, String body, String attachedPicture) throws ResourceNotFoundException {
        User commenter = userService.getById(currentUserId);
        Post post = postRepository.findById(postId).orElseThrow(() -> new ResourceNotFoundException("Post with id of " + postId + " does not exists!"));

        NotificationStatus status = userService.isModalOpen(post.getAuthor().getId(), postId, Type.COMMENT) ? NotificationStatus.READ : NotificationStatus.UNREAD;
        Comment comment = Comment.builder()
                .body(body)
                .dateCreated(LocalDateTime.now())
                .post(post)
                .commenter(commenter)
                .attachedPicture(attachedPicture)
                .notificationStatus(status)
                .status(Status.ACTIVE)
                .build();

        log.debug("Comment with body of {} saved successfully", comment.getBody());
        return commentRepository.save(comment);
    }

    Comment delete(int commentId) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new ResourceNotFoundException("Comment with id of " + commentId + " does not exists!"));
        log.debug("Comment with id of {} are now inactive!", commentId);
        return this.setStatus(comment);
    }

    public boolean isDeleted(int commentId) throws ResourceNotFoundException {
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new ResourceNotFoundException("Comment with id of " + commentId + " does not exists!"));
        return comment.getStatus() == Status.INACTIVE;
    }

    List<CommentDTO> getAllCommentsOf(int postId) throws ResourceNotFoundException, NoLoggedInUserException {
        int currentUserId = userService.getCurrentUser().getId();

        Post post = postRepository.findById(postId).orElseThrow(() -> new ResourceNotFoundException("Post with id of " + postId + " does not exists!"));
        return post.getComments()
                .stream()
                .filter(comment -> comment.getStatus() == Status.ACTIVE)
                .filter(comment -> !userService.isBlockedBy(currentUserId, comment.getCommenter().getId()))
                .filter(comment -> !userService.isYouBeenBlockedBy(currentUserId, comment.getCommenter().getId()))
                .sorted(Comparator.comparingInt(Comment::getUpvote).reversed())
                .map(commentMapper::toDTO)
                .toList();
    }

    public CommentDTO getById(int commentId) throws ResourceNotFoundException {
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new ResourceNotFoundException("Comment with id of " + commentId + " does not exists!"));
        return commentMapper.toDTO(comment);
    }

    public List<CommentDTO> getAllUnreadComments(int authorId, int postId) throws ResourceNotFoundException {
        User author = userService.getById(authorId);
        Post post = author.getPosts().stream().filter(userPost -> userPost.getId() == postId).findFirst().orElseThrow(() -> new ResourceNotFoundException("Author with id of " + authorId + " does not have post with id of " + postId));
        return post.getComments()
                .stream()
                .filter(comment -> comment.getStatus() == Status.ACTIVE)
                .filter(comment -> comment.getNotificationStatus() == NotificationStatus.UNREAD)
                .filter(comment -> !userService.isBlockedBy(authorId, comment.getCommenter().getId()))
                .filter(comment -> !userService.isYouBeenBlockedBy(authorId, comment.getCommenter().getId()))
                .map(commentMapper::toDTO)
                .toList();
    }

    public int getNotificationCountForRespondent(int authorId, int postId, int respondentId) throws ResourceNotFoundException {
        return (int) getAllUnreadComments(authorId, postId)
                .stream()
                .filter(comment -> comment.getCommenterId() == respondentId)
                .count();
    }

    public int getNotificationCountForSpecificPost(int authorId, int postId) throws ResourceNotFoundException {
        return getAllUnreadComments(authorId, postId).size();
    }

    public Set<Comment> getUnreadCommentsOfAllPost(int userId) throws ResourceNotFoundException {
        User user = userService.getById(userId);
        List<Post> posts = user.getPosts();

        return posts.stream()
                .map(Post::getComments)
                .flatMap(comments -> comments.stream()
                        .filter(comment -> comment.getStatus() == Status.ACTIVE)
                        .filter(comment -> !userService.isBlockedBy(userId, comment.getCommenter().getId()))
                        .filter(comment -> !userService.isYouBeenBlockedBy(userId, comment.getCommenter().getId()))
                        .filter(comment -> comment.getNotificationStatus() == NotificationStatus.UNREAD))
                .collect(Collectors.toSet());
    }

    CommentDTO updateUpvote(int respondentId, int commentId, int newUpvoteCount) throws ResourceNotFoundException {
        this.setUpvote(respondentId, commentId, newUpvoteCount);

        log.debug("User with id of {} upvoted the Comment with id of {} successfully with new upvote count of {} ", respondentId, commentId, newUpvoteCount);
        return this.getById(commentId);
    }

    Comment updateCommentBody(int commentId, String newBody) throws ResourceNotFoundException {
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new ResourceNotFoundException("Comment with id of " + commentId + " does not exists!"));
        if (comment.getBody().equals(newBody)) return comment;
        comment.setBody(newBody);
        commentRepository.save(comment);
        log.debug("Comment with id of {} updated with the new body of {}", commentId, newBody);
        return comment;
    }

    private void readComment(int commentId) throws ResourceNotFoundException {
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new ResourceNotFoundException("Comment with id of " + commentId + " does not exists!"));
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
                .filter(comment -> !userService.isBlockedBy(currentUserId, comment.getCommenter().getId()))
                .filter(comment -> !userService.isYouBeenBlockedBy(currentUserId, comment.getCommenter().getId()))
                .map(Comment::getId)
                .forEach(this::readComment);
    }

    boolean isNotValidUpvoteValue(int oldUpvoteCount, int newUpvoteCount) {
        int next = newUpvoteCount + 1;
        int previous = newUpvoteCount - 1;
        return oldUpvoteCount != next && oldUpvoteCount != previous;
    }

    boolean isUserAlreadyUpvoteComment(int respondentId, int commentId) throws ResourceNotFoundException {
        User respondent = userService.getById(respondentId);
        return respondent.getUpvotedComments()
                .stream()
                .anyMatch(upvotedComment -> upvotedComment.getId() == commentId);
    }

    boolean isCommentSectionClosed(int commentId) throws ResourceNotFoundException {
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new ResourceNotFoundException("Comment with id of " + commentId + " does not exists!"));
        Post post = comment.getPost();
        return post.getCommentSectionStatus() == Post.CommentSectionStatus.CLOSED;
    }

    Comment setStatus(Comment comment) throws ResourceNotFoundException {
        comment.setStatus(Status.INACTIVE);
        commentRepository.save(comment);

        comment.getReplies().forEach(replyService::setStatus);
        return comment;
    }

    private void setUpvote(int respondentId, int commentId, int newUpvoteCount) throws ResourceNotFoundException {
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new ResourceNotFoundException("Comment with id of " + commentId + " does not exists!"));
        comment.setUpvote(newUpvoteCount);
        commentRepository.save(comment);

        User respondent = userService.getById(respondentId);
        respondent.getUpvotedComments().add(comment);
        userService.save(respondent);
    }

    public int getTotalReplies(Comment comment) {
        if (comment.getReplies() == null) comment.setReplies(new ArrayList<>());
        return (int) comment.getReplies().stream()
                .filter(reply -> reply.getStatus() == Status.ACTIVE)
                .count();
    }
}
