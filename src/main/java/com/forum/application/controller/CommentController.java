package com.forum.application.controller;

import com.forum.application.dto.CommentDTO;
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
@RequestMapping("/forum/api/posts/{postId}/comments")
public class CommentController {

    private final ForumService forumService;
    private final UserService userService;

    @GetMapping
    public List<CommentDTO> getAllCommentsOf(@PathVariable("postId") int postId) {
        return forumService.getAllCommentsOf(postId);
    }

    @GetMapping("/{commentId}")
    public CommentDTO getById(@PathVariable("commentId") int commentId) {
        return forumService.getCommentById(commentId);
    }

    @PostMapping
    public ResponseEntity<?> saveComment(@PathVariable("postId") int postId,
                                         @RequestParam("body") String body,
                                         HttpSession session) {

        if (forumService.isEmpty(body)) return ResponseEntity.badRequest().body("Comment body cannot be empty!");

        String email = (String) session.getAttribute("email");
        int commenterId = userService.getIdByEmail(email);

        int commentId = forumService.saveComment(commenterId, postId, body);
        CommentDTO fetchedComment = forumService.getCommentById(commentId);
        return ResponseEntity.ok(fetchedComment);
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<CommentDTO> delete(@PathVariable("commentId") int commentId) {
        forumService.deleteComment(commentId);
        return ResponseEntity.notFound().build();
    }
}
