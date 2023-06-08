package com.forum.application.service;

import com.forum.application.dto.CommentDTO;
import com.forum.application.exception.ResourceNotFoundException;
import com.forum.application.model.*;
import com.forum.application.repository.CommentRepository;
import com.forum.application.repository.CommentUpvoteTransactionRepository;
import com.forum.application.repository.PostRepository;
import com.forum.application.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class CommentService {
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final CommentUpvoteTransactionService commentUpvoteTransactionService;

    public int save(int commenterId, int postId, String body) {
        User commenter = userRepository.findById(commenterId).orElseThrow(() -> new ResourceNotFoundException("User with id of " + commenterId + " does not exists!"));
        Post post = postRepository.findById(postId).orElseThrow(() -> new ResourceNotFoundException("Post with id of " + postId + " does not exists!"));

        Comment comment = Comment.builder()
                .body(body)
                .dateCreated(LocalDateTime.now())
                .post(post)
                .commenter(commenter)
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

    public List<CommentDTO> getAllCommentsOf(int postId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new ResourceNotFoundException("Post with id of " + postId + " does not exists!"));
        return post.getComments()
                .stream()
                .filter(p -> p.getStatus() == Status.ACTIVE)
                .sorted(Comparator.comparingInt(Comment::getUpvote).reversed())
                .map(this::convertToDTO)
                .toList();
    }

    public CommentDTO getById(int commentId) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new ResourceNotFoundException("Comment with id of " + commentId + " does not exists!"));
        return this.convertToDTO(comment);
    }

    public CommentDTO updateUpvote(int respondentId, int commentId, int newUpvoteCount) {
        this.setUpvote(respondentId, commentId, newUpvoteCount);

        CommentDTO commentDTO = this.getById(commentId);
        log.debug("User with id of {} upvoted the Comment with id of {} successfully with new upvote count of {} ", respondentId, commentId, commentDTO.getUpvote());
        return commentDTO;
    }

    public boolean isNotValidUpvoteValue(int oldUpvoteCount, int newUpvoteCount) {
        int next = newUpvoteCount + 1;
        int previous = newUpvoteCount - 1;
        return oldUpvoteCount != next && oldUpvoteCount != previous;
    }

    private CommentDTO convertToDTO(Comment comment) {
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
                .build();
    }

    private void setStatus(int commentId) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new ResourceNotFoundException("Comment with id of " + commentId + " does not exists!"));
        comment.setStatus(Status.INACTIVE);
        commentRepository.save(comment);
    }

    private void setUpvote(int respondentId, int commentId, int newUpvoteCount) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new ResourceNotFoundException("Comment with id of " + commentId + " does not exists!"));
        comment.setUpvote(newUpvoteCount);
        commentRepository.save(comment);
        commentUpvoteTransactionService.save(respondentId, commentId);
    }
}
