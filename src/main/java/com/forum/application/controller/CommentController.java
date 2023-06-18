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
        if (forumService.isPostDeleted(postId)) return ResponseEntity.badRequest().body("The post you trying to comment is either be deleted or does not exists anymore!");

        String email = (String) session.getAttribute("email");
        int commenterId = userService.getIdByEmail(email);

        int commentId = forumService.saveComment(commenterId, postId, body);
        CommentDTO fetchedComment = forumService.getCommentById(commentId);
        return ResponseEntity.ok(fetchedComment);
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<CommentDTO> delete(@PathVariable("commentId") int commentId) {
        forumService.deleteComment(commentId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("upvote/{commentId}")
    public ResponseEntity<?> updateCommentUpvote(@PathVariable("commentId") int commentId,
                                                 @RequestParam("newUpvoteCount") int newUpvoteCount,
                                                 HttpSession session) {

        if (forumService.isCommentDeleted(commentId)) return ResponseEntity.badRequest().body("The comment you trying to upvote might be deleted by the author or does not exists anymore!");

        String loginEmailSession = (String) session.getAttribute("email");
        int respondentId = userService.getIdByEmail(loginEmailSession);

        if (forumService.isUserAlreadyUpvoteComment(respondentId, commentId)) return ResponseEntity.badRequest().body("You can only up vote and down vote a comment once!");
        if (forumService.isNotValidUpvoteValue(commentId, newUpvoteCount)) return ResponseEntity.unprocessableEntity().body("Cannot update upvote count! Because new upvote count must only be + 1 or - 1 to the previous value!");
        CommentDTO commentDTO = forumService.updateUpvote(respondentId, commentId, newUpvoteCount);
        return ResponseEntity.ok(commentDTO);
    }

    @PatchMapping("/body/{commentId}")
    public ResponseEntity<?> updateCommentBody(@PathVariable("commentId") int commentId,
                                               @RequestParam("newCommentBody") String newCommentBody) {

        forumService.updateCommentBody(commentId, newCommentBody);

        CommentDTO commentDTO = forumService.getCommentById(commentId);
        return ResponseEntity.ok(commentDTO);
    }
}
