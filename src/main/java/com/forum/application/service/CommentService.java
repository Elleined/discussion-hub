package com.forum.application.service;

import com.forum.application.dto.CommentDTO;
import com.forum.application.exception.ResourceNotFoundException;
import com.forum.application.model.*;
import com.forum.application.repository.CommentRepository;
import com.forum.application.repository.PostRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class CommentService {
    private final UserService userService;
    private final PostRepository postRepository;
    private final ReplyService replyService;
    private final CommentRepository commentRepository;
    private final HttpSession session;

    public int save(int commenterId, int postId, String body) {
        User commenter = userService.getById(commenterId);
        Post post = postRepository.findById(postId).orElseThrow(() -> new ResourceNotFoundException("Post with id of " + postId + " does not exists!"));

        NotificationStatus status = userService.isModalOpen(post.getAuthor().getId(), postId, Type.COMMENT) ? NotificationStatus.READ : NotificationStatus.UNREAD;
        Comment comment = Comment.builder()
                .body(body)
                .dateCreated(LocalDateTime.now())
                .post(post)
                .commenter(commenter)
                .notificationStatus(status)
                .status(Status.ACTIVE)
                .build();

        commentRepository.save(comment);
        log.debug("Comment with body of {} saved successfully", comment.getBody());
        return comment.getId();
    }

    public void delete(int commentId) {
        this.setStatus(commentId);
        log.debug("Comment with id of {} are now inactive!", commentId);
    }

    public boolean isDeleted(int commentId) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new ResourceNotFoundException("Comment with id of " + commentId + " does not exists!"));
        return comment.getStatus() == Status.INACTIVE;
    }

    public List<CommentDTO> getAllCommentsOf(int postId) {
        String loginEmailSession = (String) session.getAttribute("email");
        int userId = userService.getIdByEmail(loginEmailSession);

        Post post = postRepository.findById(postId).orElseThrow(() -> new ResourceNotFoundException("Post with id of " + postId + " does not exists!"));
        return post.getComments()
                .stream()
                .filter(comment -> comment.getStatus() == Status.ACTIVE)
                .filter(comment -> !userService.isBlockedBy(userId, comment.getCommenter().getId()))
                .filter(comment -> !userService.isYouBeenBlockedBy(userId, comment.getCommenter().getId()))
                .sorted(Comparator.comparingInt(Comment::getUpvote).reversed())
                .map(this::convertToDTO)
                .toList();
    }

    public CommentDTO getById(int commentId) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new ResourceNotFoundException("Comment with id of " + commentId + " does not exists!"));
        return this.convertToDTO(comment);
    }

    public List<CommentDTO> getAllUnreadCommentsOfSpecificPostById(int authorId, int postId) {
        User author = userService.getById(authorId);
        Post post = author.getPosts().stream().filter(userPost -> userPost.getId() == postId).findFirst().orElseThrow(() -> new ResourceNotFoundException("Author with id of " + authorId + " does not have post with id of " + postId));
        return post.getComments()
                .stream()
                .filter(comment -> comment.getStatus() == Status.ACTIVE)
                .filter(comment -> comment.getNotificationStatus() == NotificationStatus.UNREAD)
                .filter(comment -> !userService.isBlockedBy(authorId, comment.getCommenter().getId()))
                .filter(comment -> !userService.isYouBeenBlockedBy(authorId, comment.getCommenter().getId()))
                .map(this::convertToDTO)
                .toList();
    }

    public List<CommentDTO> getAllUnreadCommentOfAllPostByAuthorId(int userId) {
        User user = userService.getById(userId);
        List<Post> posts = user.getPosts();

        return posts.stream()
                .map(Post::getComments)
                .flatMap(comments -> comments.stream()
                        .filter(comment -> comment.getStatus() == Status.ACTIVE)
                        .filter(comment -> !userService.isBlockedBy(userId, comment.getCommenter().getId()))
                        .filter(comment -> !userService.isYouBeenBlockedBy(userId, comment.getCommenter().getId()))
                        .filter(comment -> comment.getNotificationStatus() == NotificationStatus.UNREAD))
                .map(this::convertToDTO)
                .toList();
    }

    public long getAllUnreadCommentsCount(int userId) {
        return getAllUnreadCommentOfAllPostByAuthorId(userId).size();
    }

    public int getNotificationCountForRespondent(int authorId, int postId, int respondentId) {
        return (int) getAllUnreadCommentsOfSpecificPostById(authorId, postId)
                .stream()
                .filter(comment -> comment.getCommenterId() == respondentId)
                .count();
    }

    public int getNotificationCountForSpecificPost(int authorId, int postId) {
        return getAllUnreadCommentsOfSpecificPostById(authorId, postId).size();
    }


    public CommentDTO updateUpvote(int respondentId, int commentId, int newUpvoteCount) {
        this.setUpvote(respondentId, commentId, newUpvoteCount);

        log.debug("User with id of {} upvoted the Comment with id of {} successfully with new upvote count of {} ", respondentId, commentId, newUpvoteCount);
        return this.getById(commentId);
    }

    public void updateCommentBody(int commentId, String newBody) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new ResourceNotFoundException("Comment with id of " + commentId + " does not exists!"));
        if (comment.getBody().equals(newBody)) return; // Returning if user doesn't change the comment body
        comment.setBody(newBody);
        commentRepository.save(comment);
        log.debug("Comment with id of {} updated with the new body of {}", commentId, newBody);
    }

    private void updateNotificationStatus(int commentId, NotificationStatus newStatus) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new ResourceNotFoundException("Comment with id of " + commentId + " does not exists!"));
        comment.setNotificationStatus(newStatus);
        commentRepository.save(comment);
        log.debug("Comment with id of {} notification status updated to {}", commentId, newStatus);
    }

    public void updateAllCommentNotificationStatusByPostId(int postId, NotificationStatus newStatus) {
        String loginEmailSession = (String) session.getAttribute("email");
        int userId = userService.getIdByEmail(loginEmailSession);

        Post post = postRepository.findById(postId).orElseThrow(() -> new ResourceNotFoundException("Post with id of " + postId + " does not exists!"));
        if (userId != post.getAuthor().getId()) {
            log.debug("Will not mark as unread because the current user with id of {} are not the author of the post who is {}", userId, post.getAuthor().getId());
            return;
        }
        log.debug("Will mark all as read becuase the current user with id of {} is the author of the post {}", userId, post.getAuthor().getId());
        post.getComments()
                .stream()
                .filter(comment -> comment.getStatus() == Status.ACTIVE)
                .filter(comment -> !userService.isBlockedBy(userId, comment.getCommenter().getId()))
                .filter(comment -> !userService.isYouBeenBlockedBy(userId, comment.getCommenter().getId()))
                .map(Comment::getId)
                .forEach(commentId -> this.updateNotificationStatus(commentId, newStatus));
    }

    boolean isNotValidUpvoteValue(int oldUpvoteCount, int newUpvoteCount) {
        int next = newUpvoteCount + 1;
        int previous = newUpvoteCount - 1;
        return oldUpvoteCount != next && oldUpvoteCount != previous;
    }

    public boolean isUserAlreadyUpvoteComment(int respondentId, int commentId) {
        User respondent = userService.getById(respondentId);
        return respondent.getUpvotedComments()
                .stream()
                .anyMatch(upvotedComment -> upvotedComment.getId() == commentId);
    }

    CommentDTO convertToDTO(Comment comment) {
        if (comment.getReplies() == null) comment.setReplies(new ArrayList<>());
        return CommentDTO.builder()
                .id(comment.getId())
                .body(comment.getBody())
                .dateCreated(comment.getDateCreated())
                .formattedDate(Formatter.formatDateWithoutYear(comment.getDateCreated()))
                .formattedTime(Formatter.formatTime(comment.getDateCreated()))
                .commenterName(comment.getCommenter().getName())
                .postId(comment.getPost().getId())
                .commenterId(comment.getCommenter().getId())
                .commenterPicture(comment.getCommenter().getPicture())
                .authorName(comment.getPost().getAuthor().getName())
                .upvote(comment.getUpvote())
                .status(comment.getStatus().name())
                .totalReplies((int) comment.getReplies().stream()
                        .filter(reply -> reply.getStatus() == Status.ACTIVE)
                        .count())
                .notificationStatus(comment.getNotificationStatus().name())
                .build();
    }

    void setStatus(int commentId) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new ResourceNotFoundException("Comment with id of " + commentId + " does not exists!"));
        comment.setStatus(Status.INACTIVE);
        commentRepository.save(comment);

        comment.getReplies()
                .stream()
                .map(Reply::getId)
                .forEach(replyService::setStatus);
    }

    private void setUpvote(int respondentId, int commentId, int newUpvoteCount) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new ResourceNotFoundException("Comment with id of " + commentId + " does not exists!"));
        comment.setUpvote(newUpvoteCount);
        commentRepository.save(comment);

        User respondent = userService.getById(respondentId);
        respondent.getUpvotedComments().add(comment);
        userService.save(respondent);
    }
}
