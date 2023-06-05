package com.forum.application.service;

import com.forum.application.dto.PostDTO;
import com.forum.application.exception.ResourceNotFoundException;
import com.forum.application.model.Post;
import com.forum.application.model.User;
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
public class PostService {
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    public int save(int authorId, String body) {
        User author = userRepository.findById(authorId).orElseThrow(() -> new ResourceNotFoundException("User with id of " + authorId + " does not exists!"));

        Post post = Post.builder()
                .body(body)
                .dateCreated(LocalDateTime.now())
                .author(author)
                .build();

        postRepository.save(post);
        log.debug("Post with body of {} saved successfully!", post.getBody());
        return post.getId();
    }

    public void delete(int postId) {
        postRepository.deleteById(postId);
        log.debug("Post with id of {} deleted successfully", postId);
    }

    public List<PostDTO> getAll() {
        return postRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .sorted(Comparator.comparing(PostDTO::getDateCreated).reversed())
                .toList();
    }

    public PostDTO convertToDTO(Post post) {
        return PostDTO.builder()
                .id(post.getId())
                .body(post.getBody())
                .dateCreated(post.getDateCreated())
                .formattedDateCreated(Formatter.formatDateWithoutYear(post.getDateCreated()))
                .formattedTimeCreated(Formatter.formatTime(post.getDateCreated()))
                .authorName(post.getAuthor().getName())
                .authorId(post.getAuthor().getId())
                .build();
    }
}
