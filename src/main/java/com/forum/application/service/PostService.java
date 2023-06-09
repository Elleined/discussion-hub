package com.forum.application.service;

import com.forum.application.dto.PostDTO;
import com.forum.application.exception.ResourceNotFoundException;
import com.forum.application.model.*;
import com.forum.application.repository.PostRepository;
import com.forum.application.repository.UserRepository;
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

    private final UserRepository userRepository;
    private final PostRepository postRepository;

    private final CommentService commentService;
    private final ReplyService replyService;

    public int save(int authorId, String body) {
        User author = userRepository.findById(authorId).orElseThrow(() -> new ResourceNotFoundException("User with id of " + authorId + " does not exists!"));

        Post post = Post.builder()
                .body(body)
                .dateCreated(LocalDateTime.now())
                .author(author)
                .status(Status.ACTIVE)
                .build();

        postRepository.save(post);
        log.debug("Post with body of {} saved successfully!", post.getBody());
        return post.getId();
    }

    public void delete(int postId) {
        this.setStatus(postId);
        log.debug("Post with id of {} are now inactive", postId);
    }

    public List<PostDTO> getAll() {
        return postRepository.findAll()
                .stream()
                .filter(post -> post.getStatus() == Status.ACTIVE)
                .map(this::convertToDTO)
                .sorted(Comparator.comparing(PostDTO::getDateCreated).reversed())
                .toList();
    }

    public PostDTO getById(int postId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new ResourceNotFoundException("Post with id of " + postId + " does not exists!"));
        return this.convertToDTO(post);
    }

    public List<PostDTO> getAllByAuthorId(int authorId) {
        if (!userRepository.existsById(authorId)) throw new ResourceNotFoundException("User with id of " + authorId + " does not exists");
        return postRepository.fetchAllByAuthorId(authorId)
                .stream()
                .filter(post -> post.getStatus() == Status.ACTIVE)
                .map(this::convertToDTO)
                .sorted(Comparator.comparing(PostDTO::getDateCreated).reversed())
                .toList();
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
