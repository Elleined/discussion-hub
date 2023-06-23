package com.forum.application.service;

import com.forum.application.dto.PostDTO;
import com.forum.application.exception.ResourceNotFoundException;
import com.forum.application.model.*;
import com.forum.application.model.Post.CommentSectionStatus;
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
public class PostService {

    private final UserService userService;
    private final PostRepository postRepository;
    private final CommentService commentService;
    private final HttpSession session;

    public int save(int authorId, String body) {
        User author = userService.getById(authorId);

        Post post = Post.builder()
                .body(body)
                .dateCreated(LocalDateTime.now())
                .author(author)
                .status(Status.ACTIVE)
                .commentSectionStatus(CommentSectionStatus.OPEN)
                .build();

        postRepository.save(post);
        log.debug("Post with body of {} saved successfully!", post.getBody());
        return post.getId();
    }

    public void delete(int postId) {
        this.setStatus(postId);
        log.debug("Post with id of {} are now inactive", postId);
    }

    public void updatePostBody(int postId, String newBody) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new ResourceNotFoundException("Post with id of " + postId + " does not exists!"));
        if (post.getBody().equals(newBody)) return; // Returning if user doesn't change the post body
        post.setBody(newBody);
        postRepository.save(post);
        log.debug("Post with id of {} updated with the new body of {}", postId, newBody);
    }

    public void updateCommentSectionStatus(int postId, CommentSectionStatus status) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new ResourceNotFoundException("Post with id of " + postId + " does not exists!"));
        post.setCommentSectionStatus(status);
        postRepository.save(post);
        log.debug("Comment section of Post with id of {} are now {}", postId, post.getCommentSectionStatus().name());
    }

    public void batchUpdateOfCommentsNotificationStatusByPostId(int postId, NotificationStatus newStatus) {
        String loginEmailSession = (String) session.getAttribute("email");
        int userId = userService.getIdByEmail(loginEmailSession);

        Post post = postRepository.findById(postId).orElseThrow(() -> new ResourceNotFoundException("Post with id of " + postId + " does not exists!"));
        post.getComments()
                .stream()
                .filter(comment -> comment.getStatus() == Status.ACTIVE)
                .filter(comment -> !userService.isBlockedBy(userId, comment.getCommenter().getId()))
                .filter(comment -> !userService.isYouBeenBlockedBy(userId, comment.getCommenter().getId()))
                .map(Comment::getId)
                .forEach(commentId -> commentService.updateNotificationStatus(commentId, newStatus));
    }
    public boolean isDeleted(int postId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new ResourceNotFoundException("Post with id of " + postId + " does not exists!"));
        return post.getStatus() == Status.INACTIVE;
    }

    public PostDTO getById(int postId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new ResourceNotFoundException("Post with id of " + postId + " does not exists!"));
        return this.convertToDTO(post);
    }

    public List<PostDTO> getAll() {
        String loginEmailSession = (String) session.getAttribute("email");
        int userId = userService.getIdByEmail(loginEmailSession);

        return postRepository.findAll()
                .stream()
                .filter(post -> post.getStatus() == Status.ACTIVE)
                .filter(post -> !userService.isBlockedBy(userId, post.getAuthor().getId()))
                .filter(post -> !userService.isYouBeenBlockedBy(userId, post.getAuthor().getId()))
                .sorted(Comparator.comparing(Post::getDateCreated).reversed())
                .map(this::convertToDTO)
                .toList();
    }

    public List<PostDTO> getAllByAuthorId(int authorId) {
        if (!userService.existsById(authorId)) throw new ResourceNotFoundException("User with id of " + authorId + " does not exists");
        return postRepository.fetchAllByAuthorId(authorId)
                .stream()
                .filter(post -> post.getStatus() == Status.ACTIVE)
                .map(this::convertToDTO)
                .sorted(Comparator.comparing(PostDTO::getDateCreated).reversed())
                .toList();
    }

    public String getCommentSectionStatus(int postId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new ResourceNotFoundException("Post with id of " + postId + " does not exists!"));
        return post.getCommentSectionStatus().name();
    }


    public PostDTO convertToDTO(Post post) {
        if (post.getComments() == null) post.setComments(new ArrayList<>());
        int totalCommentAndReplies = this.getTotalCommentsAndReplies(post);
        return PostDTO.builder()
                .id(post.getId())
                .body(post.getBody())
                .dateCreated(post.getDateCreated())
                .formattedDateCreated(Formatter.formatDateWithoutYear(post.getDateCreated()))
                .formattedTimeCreated(Formatter.formatTime(post.getDateCreated()))
                .authorName(post.getAuthor().getName())
                .authorId(post.getAuthor().getId())
                .authorPicture(post.getAuthor().getPicture())
                .totalCommentAndReplies(totalCommentAndReplies)
                .status(post.getStatus().name())
                .commentSectionStatus(post.getCommentSectionStatus().name())
                .build();
    }

    private void setStatus(int postId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new ResourceNotFoundException("Comment with id of " + postId + " does not exists!"));
        post.setStatus(Status.INACTIVE);
        postRepository.save(post);

        post.getComments()
                .stream()
                .map(Comment::getId)
                .forEach(commentService::setStatus);
    }

    private int getTotalCommentsAndReplies(Post post) {
        int commentCount = (int) post.getComments()
                .stream()
                .filter(comment -> comment.getStatus() == Status.ACTIVE)
                .count();

        int commentRepliesCount = (int) post.getComments()
                .stream()
                .map(Comment::getReplies)
                .flatMap(replies -> replies.stream()
                        .filter(reply -> reply.getStatus() == Status.ACTIVE))
                .count();

        return commentCount + commentRepliesCount;
    }
}
