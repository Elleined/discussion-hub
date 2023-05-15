package com.forum.application.controller;

import com.forum.application.dto.ReplyDTO;
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
@RequestMapping("/forum/api/posts/comments/{commentId}/replies")
public class ReplyController {

    private final ForumService forumService;
    private final UserService userService;

    public ReplyController(ForumService forumService, UserService userService) {
        this.forumService = forumService;
        this.userService = userService;
    }

    @GetMapping
    public List<ReplyDTO> getAllRepliesOf(@PathVariable int commentId) {
        return forumService.getAllRepliesOf(commentId);
    }

    @PostMapping
    public ResponseEntity<?> saveReply(@PathVariable int commentId,
                                       @RequestParam String body,
                                       HttpSession session) {

        if (body == null || body.isEmpty() || body.isBlank()) return ResponseEntity.badRequest().body("Reply body cannot be empty!");

        String email = (String) session.getAttribute("email");
        int replierId = userService.getIdByEmail(email);

        forumService.saveReply(replierId, commentId, body);
        log.debug("Reply saved successfully");
        return ResponseEntity.status(200).body("Replied Successfully");
    }

    @DeleteMapping("/{replyId}")
    public ResponseEntity<?> delete(@PathVariable int replyId) {
        forumService.deleteReply(replyId);
        log.debug("Reply deleted successfully");
        return ResponseEntity.status(204).body("Reply Deleted Successfully");
    }
}
