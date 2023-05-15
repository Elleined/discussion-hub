package com.forum.application.controller;

import com.forum.application.dto.CommentDTO;
import com.forum.application.service.ForumService;
import com.forum.application.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/forum/api/posts/{postId}/comments")
public class CommentController {

    private final ForumService forumService;
    private final UserService userService;

    public CommentController(ForumService forumService, UserService userService) {
        this.forumService = forumService;
        this.userService = userService;
    }

    @GetMapping
    public List<CommentDTO> getAllCommentsOf(@PathVariable int postId) {
        return forumService.getAllCommentsOf(postId);
    }

    @PostMapping
    public ResponseEntity<?> saveComment(@PathVariable int postId,
                                         @RequestParam String body,
                                         HttpSession session) {

        String email = (String) session.getAttribute("email");
        int commenterId = userService.getIdByEmail(email);

        forumService.saveComment(commenterId, postId, body);
        log.debug("Comment saved successfully");
        return ResponseEntity.status(HttpStatusCode.valueOf(200)).body("Commented Successfully");
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<?> delete(@PathVariable int commentId) {
        forumService.deleteComment(commentId);
        log.debug("Comment deleted successfully");
        return ResponseEntity.status(204).body("Comment Deleted Successfully");
    }
}
