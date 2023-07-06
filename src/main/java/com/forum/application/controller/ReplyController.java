package com.forum.application.controller;

import com.forum.application.dto.ReplyDTO;
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
@RequestMapping("/forum/api/posts/comments/{commentId}/replies")
public class ReplyController {

    private final ForumService forumService;

    @GetMapping
    public List<ReplyDTO> getAllRepliesOf(@PathVariable("commentId") int commentId) {
        return forumService.getAllRepliesOf(commentId);
    }

    @GetMapping("/{replyId}")
    public ReplyDTO getById(@PathVariable("replyId") int replyId) {
        return forumService.getReplyById(replyId);
    }

    @PostMapping
    public ResponseEntity<ReplyDTO> saveReply(@PathVariable("commentId") int commentId,
                                       @RequestParam("body") String body,
                                       @RequestParam(required = false, name = "mentionedUserIds") Set<Integer> mentionedUserIds) {

        ReplyDTO replyDTO = forumService.saveReply(commentId, body, mentionedUserIds);
        return ResponseEntity.ok (replyDTO );
    }

    @DeleteMapping("/{replyId}")
    public ResponseEntity<ReplyDTO> delete(@PathVariable("replyId") int replyId) {
        forumService.deleteReply(replyId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/body/{replyId}")
    public ResponseEntity<ReplyDTO> updateReplyBody(@PathVariable("replyId") int replyId,
                                                    @RequestParam("newReplyBody") String newReplyBody) {

        ReplyDTO replyDTO = forumService.updateReplyBody(replyId, newReplyBody);
        return ResponseEntity.ok( replyDTO );
    }
}
