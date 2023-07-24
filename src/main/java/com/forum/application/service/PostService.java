package com.forum.application.service;

import com.forum.application.exception.ResourceNotFoundException;
import com.forum.application.model.Comment;
import com.forum.application.model.Post;
import com.forum.application.model.Post.CommentSectionStatus;
import com.forum.application.model.Status;
import com.forum.application.model.User;
import com.forum.application.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class PostService {

    private final UserService userService;
    private final BlockService blockService;
    private final PostRepository postRepository;
    private final CommentService commentService;

    Post save(int authorId, String body) throws ResourceNotFoundException {
        User author = userService.getById(authorId);

        Post post = Post.builder()
                .body(body)
                .dateCreated(LocalDateTime.now())
                .author(author)
                .status(Status.ACTIVE)
                .commentSectionStatus(CommentSectionStatus.OPEN)
                .build();

        log.debug("Post with body of {} saved successfully!", post.getBody());
        return postRepository.save(post);
    }

    void delete(int postId) {
        this.setStatus(postId);
        log.debug("Post with id of {} are now inactive", postId);
    }

    Post updatePostBody(int postId, String newBody) throws ResourceNotFoundException {
        Post post = postRepository.findById(postId).orElseThrow(() -> new ResourceNotFoundException("Post with id of " + postId + " does not exists!"));
        if (post.getBody().equals(newBody)) return post; // Returning if user doesn't change the post body
        post.setBody(newBody);
        log.debug("Post with id of {} updated with the new body of {}", postId, newBody);
        return postRepository.save(post);
    }

    Post updateCommentSectionStatus(int postId, CommentSectionStatus status) throws ResourceNotFoundException {
        Post post = postRepository.findById(postId).orElseThrow(() -> new ResourceNotFoundException("Post with id of " + postId + " does not exists!"));
        post.setCommentSectionStatus(status);
        log.debug("Comment section of Post with id of {} are now {}", postId, post.getCommentSectionStatus().name());
        return postRepository.save(post);
    }

    public Post getById(int postId) throws ResourceNotFoundException {
        return postRepository.findById(postId).orElseThrow(() -> new ResourceNotFoundException("Post with id of " + postId + " does not exists!"));
    }

    List<Post> getAll() {
        int currentUserId = userService.getCurrentUser().getId();

        return postRepository.findAll()
                .stream()
                .filter(post -> post.getStatus() == Status.ACTIVE)
                .filter(post -> !blockService.isBlockedBy(currentUserId, post.getAuthor().getId()))
                .filter(post -> !blockService.isYouBeenBlockedBy(currentUserId, post.getAuthor().getId()))
                .sorted(Comparator.comparing(Post::getDateCreated).reversed())
                .toList();
    }

    List<Post> getAllByAuthorId(int authorId) throws ResourceNotFoundException {
        if (!userService.existsById(authorId)) throw new ResourceNotFoundException("User with id of " + authorId + " does not exists");
        return postRepository.fetchAllByAuthorId(authorId)
                .stream()
                .filter(post -> post.getStatus() == Status.ACTIVE)
                .sorted(Comparator.comparing(Post::getDateCreated).reversed())
                .toList();
    }

    boolean isCommentSectionClosed(int postId) throws ResourceNotFoundException {
        Post post = postRepository.findById(postId).orElseThrow(() -> new ResourceNotFoundException("Post with id of " + postId + " does not exists!"));
        return post.getCommentSectionStatus() == CommentSectionStatus.CLOSED;
    }

    boolean isDeleted(int postId) throws ResourceNotFoundException {
        Post post = postRepository.findById(postId).orElseThrow(() -> new ResourceNotFoundException("Post with id of " + postId + " does not exists!"));
        return post.getStatus() == Status.INACTIVE;
    }

    public String getCommentSectionStatus(int postId) throws ResourceNotFoundException {
        Post post = postRepository.findById(postId).orElseThrow(() -> new ResourceNotFoundException("Post with id of " + postId + " does not exists!"));
        return post.getCommentSectionStatus().name();
    }

    public int getTotalCommentsAndReplies(Post post) {
        int currentUserId = userService.getCurrentUser().getId();
        int commentCount = (int) post.getComments()
                .stream()
                .filter(comment -> comment.getStatus() == Status.ACTIVE)
                .filter(comment -> !blockService.isBlockedBy(currentUserId, comment.getCommenter().getId()))
                .filter(comment -> !blockService.isYouBeenBlockedBy(currentUserId, comment.getCommenter().getId()))
                .count();

        int commentRepliesCount = (int) post.getComments()
                .stream()
                .map(Comment::getReplies)
                .flatMap(replies -> replies.stream()
                        .filter(reply -> reply.getStatus() == Status.ACTIVE)
                        .filter(reply -> !blockService.isBlockedBy(currentUserId, reply.getReplier().getId()))
                        .filter(reply -> !blockService.isYouBeenBlockedBy(currentUserId, reply.getReplier().getId())))
                .count();

        return commentCount + commentRepliesCount;
    }

    private void setStatus(int postId) throws ResourceNotFoundException {
        Post post = postRepository.findById(postId).orElseThrow(() -> new ResourceNotFoundException("Comment with id of " + postId + " does not exists!"));
        post.setStatus(Status.INACTIVE);
        post.getComments().forEach(commentService::setStatus);
    }

}
