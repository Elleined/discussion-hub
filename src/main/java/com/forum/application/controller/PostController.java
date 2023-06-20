package com.forum.application.controller;

import com.forum.application.dto.PostDTO;
import com.forum.application.model.Post;
import com.forum.application.service.ForumService;
import com.forum.application.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/forum/api/posts")
public class PostController {
    private final ForumService forumService;
    private final UserService userService;


    @GetMapping
    public List<PostDTO> getAllPost() {
        return forumService.getAllPost();
    }

    @GetMapping("/{postId}")
    public PostDTO getById(@PathVariable("postId") int postId) {
        return forumService.getPostById(postId);
    }

    @GetMapping("/author/{id}")
    public List<PostDTO> getAllByAuthorId(@PathVariable("id") int authorId) {
        return forumService.getAllByAuthorId(authorId);
    }

    @GetMapping("/{postId}/commentSectionStatus")
    public String getCommentSectionStatus(@PathVariable("postId") int postId) {
        return forumService.getCommentSectionStatus(postId);
    }

    @PostMapping
    public ResponseEntity<?> savePost(@RequestParam("body") String body,
                                      HttpSession session) {

        if (forumService.isEmpty(body)) return ResponseEntity.badRequest().body("Post body cannot be empty!");

        String loginEmailSession = (String) session.getAttribute("email");
        int authorId = userService.getIdByEmail(loginEmailSession);

        int postId = forumService.savePost(authorId, body);
        PostDTO postDTO = forumService.getPostById(postId);
        return ResponseEntity.ok(postDTO);
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<PostDTO> deletePost(@PathVariable("postId") int postId) {
        forumService.deletePost(postId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{postId}/commentSectionStatus")
    public ResponseEntity<PostDTO> updateCommentSectionStatus(@PathVariable("postId") int postId,
                                                              @RequestParam("newStatus") Post.CommentSectionStatus status) {

        forumService.updateCommentSectionStatus(postId, status);

        PostDTO postDTO = forumService.getPostById(postId);
        return ResponseEntity.ok(postDTO);
    }
}
