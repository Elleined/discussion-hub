package com.forum.application.controller;

import com.forum.application.dto.CommentDTO;
import com.forum.application.model.Type;
import com.forum.application.service.ForumService;
import com.forum.application.service.UserService;
import jakarta.servlet.http.HttpSession;
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
                                         @RequestParam(required = false, name = "mentionedUserIds") Set<Integer> mentionedUserIds,
                                         HttpSession session) {

        if (forumService.isEmpty(body)) return ResponseEntity.badRequest().body("Comment body cannot be empty!");
        if (forumService.isPostCommentSectionClosed(postId)) return ResponseEntity.badRequest().body("Cannot comment because author already closed the comment section for this post!");
        if (forumService.isPostDeleted(postId)) return ResponseEntity.badRequest().body("The post you trying to comment is either be deleted or does not exists anymore!");

        int currentUserId = userService.getCurrentUser().getId();

        int authorId = forumService.getPostById(postId).getAuthorId();
        if (userService.isYouBeenBlockedBy(currentUserId, authorId)) return ResponseEntity.badRequest().body("Cannot comment because this user block you already!");

        int commentId = forumService.saveComment(currentUserId, postId, body);
        if (mentionedUserIds != null) forumService.mentionUsers(currentUserId, mentionedUserIds, Type.COMMENT, commentId); // might be bug because if post doesnt get stored this will be null

        CommentDTO fetchedComment = forumService.getCommentById(commentId);
        return ResponseEntity.ok(fetchedComment);
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<CommentDTO> delete(@PathVariable("commentId") int commentId) {
        forumService.deleteComment(commentId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/upvote/{commentId}")
    public ResponseEntity<?> updateCommentUpvote(@PathVariable("commentId") int commentId,
                                                 @RequestParam("newUpvoteCount") int newUpvoteCount,
                                                 HttpSession session) {

        if (forumService.isCommentDeleted(commentId)) return ResponseEntity.badRequest().body("The comment you trying to upvote might be deleted by the author or does not exists anymore!");

        int currentUserId = userService.getCurrentUser().getId();

        if (forumService.isUserAlreadyUpvoteComment(currentUserId, commentId)) return ResponseEntity.badRequest().body("You can only up vote and down vote a comment once!");
        if (forumService.isNotValidUpvoteValue(commentId, newUpvoteCount)) return ResponseEntity.unprocessableEntity().body("Cannot update upvote count! Because new upvote count must only be + 1 or - 1 to the previous value!");
        CommentDTO commentDTO = forumService.updateUpvote(currentUserId, commentId, newUpvoteCount);
        return ResponseEntity.ok(commentDTO);
    }

    @PatchMapping("/body/{commentId}")
    public ResponseEntity<CommentDTO> updateCommentBody(@PathVariable("commentId") int commentId,
                                               @RequestParam("newCommentBody") String newCommentBody) {
        forumService.updateCommentBody(commentId, newCommentBody);

        CommentDTO commentDTO = forumService.getCommentById(commentId);
        return ResponseEntity.ok(commentDTO);
    }
}
