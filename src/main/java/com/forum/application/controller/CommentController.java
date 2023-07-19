package com.forum.application.controller;

import com.forum.application.dto.CommentDTO;
import com.forum.application.service.ForumService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/forum/api/posts/{postId}/comments")
public class CommentController {

    private final ForumService forumService;

    @GetMapping
    public List<CommentDTO> getAllCommentsOf(@PathVariable("postId") int postId) {
        return forumService.getAllCommentsOf(postId);
    }

    @GetMapping("/{commentId}")
    public CommentDTO getById(@PathVariable("commentId") int commentId) {
        return forumService.getCommentById(commentId);
    }

    @PostMapping
    public ResponseEntity<CommentDTO> saveComment(@PathVariable("postId") int postId,
                                                  @RequestParam("body") String body,
                                                  @RequestParam(required = false, value = "attachedPicture") String attachedPicture,
                                                  @RequestParam(required = false, name = "mentionedUserIds") Set<Integer> mentionedUserIds) {

        CommentDTO commentDTO = forumService.saveComment(postId, body, attachedPicture, mentionedUserIds);
        return ResponseEntity.ok( commentDTO );
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<CommentDTO> delete(@PathVariable("commentId") int commentId) {
        forumService.deleteComment(commentId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/upvote/{commentId}")
    public ResponseEntity<CommentDTO> updateCommentUpvote(@PathVariable("commentId") int commentId,
                                                          @RequestParam("newUpvoteCount") int newUpvoteCount) {

        CommentDTO commentDTO = forumService.updateUpvote(commentId, newUpvoteCount);
        return ResponseEntity.ok( commentDTO );
    }

    @PatchMapping("/body/{commentId}")
    public ResponseEntity<CommentDTO> updateCommentBody(@PathVariable("commentId") int commentId,
                                               @RequestParam("newCommentBody") String newCommentBody) {

        CommentDTO commentDTO = forumService.updateCommentBody(commentId, newCommentBody);
        return ResponseEntity.ok( commentDTO );
    }
}
