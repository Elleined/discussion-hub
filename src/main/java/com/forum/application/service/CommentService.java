package com.forum.application.service;

import com.forum.application.dto.CommentDTO;
import com.forum.application.model.Comment;
import com.forum.application.model.Post;
import com.forum.application.model.User;
import com.forum.application.repository.CommentRepository;
import com.forum.application.repository.PostRepository;
import com.forum.application.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class CommentService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;


    @Transactional
    public void save(int commenterId, int postId, String body) {
        User commenter = userRepository.findById(commenterId).orElseThrow();
        Post post = postRepository.findById(postId).orElseThrow();

        Comment comment = Comment.builder()
                .body(body)
                .dateCreated(LocalDateTime.now())
                .post(post)
                .commenter(commenter)
                .build();

        commentRepository.save(comment);
    }

    @Transactional
    public void delete(int commentId) {
        commentRepository.deleteById(commentId);
    }

    public List<CommentDTO> getAllCommentsOf(int postId) {
        Post post = postRepository.findById(postId).orElseThrow();
        return post.getComments()
                .stream()
                .map(this::convertToDTO)
                .toList();
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
                .build();
    }
}
