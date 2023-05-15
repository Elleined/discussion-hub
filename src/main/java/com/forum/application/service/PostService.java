package com.forum.application.service;

import com.forum.application.dto.PostDTO;
import com.forum.application.model.Post;
import com.forum.application.model.User;
import com.forum.application.repository.PostRepository;
import com.forum.application.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PostService {
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    public PostService(UserRepository userRepository, PostRepository postRepository) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
    }

    @Transactional
    public void save(int authorId, String body) {
        User author = userRepository.findById(authorId).orElseThrow();

        Post post = Post.builder()
                .body(body)
                .dateCreated(LocalDateTime.now())
                .author(author)
                .build();

        postRepository.save(post);
    }

    @Transactional
    public void delete(int postId) {
        postRepository.deleteById(postId);
    }

    public List<PostDTO> getAll() {
        return postRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .toList();
    }

    public PostDTO convertToDTO(Post post) {
        return PostDTO.builder()
                .id(post.getId())
                .body(post.getBody())
                .dateCreated(post.getDateCreated())
                .formattedDateCreated(Formatter.formatDate(post.getDateCreated()))
                .authorName(post.getAuthor().getName())
                .authorId(post.getAuthor().getId())
                .build();
    }
}
