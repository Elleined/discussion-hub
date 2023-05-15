package com.forum.application.controller;

import com.forum.application.dto.PostDTO;
import com.forum.application.service.UserService;
import com.forum.application.service.ForumService;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/forum/api/posts")
public class PostController {
    private final ForumService forumService;
    private final UserService userService;

    public PostController(ForumService forumService, UserService userService) {
        this.forumService = forumService;
        this.userService = userService;
    }

    @GetMapping
    public List<PostDTO> getAllPost() {
        return forumService.getAllPost();
    }

    @PostMapping
    public ResponseEntity<?> savePost(@RequestParam String body,
                                      HttpSession session) {

        String loginEmailSession = (String) session.getAttribute("email");

        int authorId = userService.getIdByEmail(loginEmailSession);

        forumService.savePost(authorId, body);
        log.debug("Post saved successfully");
        return ResponseEntity.status(HttpStatusCode.valueOf(200)).body("Post Created Successfully");
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<?> deletePost(@PathVariable int postId) {
        forumService.deletePost(postId);
        log.debug("Post deleted successfully");
        return ResponseEntity.status(204).body("Post Deleted Successfully");
    }
}
