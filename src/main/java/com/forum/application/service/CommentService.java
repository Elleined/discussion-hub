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

        Comment comment = Comment.builder()
                .body(body)
                .dateCreated(LocalDateTime.now())
                .post(post)
                .commenter(commenter)
                .notificationStatus(NotificationStatus.UNREAD)
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

    public List<CommentDTO> getAllUnreadCommentsOf(int userId) {
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

    public void updateNotificationStatus(int commentId, NotificationStatus newStatus) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new ResourceNotFoundException("Comment with id of " + commentId + " does not exists!"));
        comment.setNotificationStatus(newStatus);
        commentRepository.save(comment);
        log.debug("Comment with id of {} notification status updated to {}", commentId, newStatus);
    }

    public void batchUpdateOfRepliesNotificationStatusByCommentId(int commentId, NotificationStatus newStatus) {
        String loginEmailSession = (String) session.getAttribute("email");
        int userId = userService.getIdByEmail(loginEmailSession);

        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new ResourceNotFoundException("Comment with id of " + commentId + " does not exists!"));
        if (userId != comment.getCommenter().getId()) {
            log.debug("Will not mark as unread because the current user are not the commenter of the comment");
            return;
        }
        comment.getReplies()
                .stream()
                .filter(reply -> reply.getStatus() == Status.ACTIVE)
                .filter(reply -> !userService.isBlockedBy(userId, reply.getReplier().getId()))
                .filter(reply -> !userService.isYouBeenBlockedBy(userId, reply.getReplier().getId()))
                .map(Reply::getId)
                .forEach(replyId -> replyService.updateNotificationStatus(replyId, newStatus));
    }
    public boolean isNotValidUpvoteValue(int oldUpvoteCount, int newUpvoteCount) {
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

    public void setStatus(int commentId) {
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
