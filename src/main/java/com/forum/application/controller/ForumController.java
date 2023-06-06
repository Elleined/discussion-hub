package com.forum.application.controller;

import com.forum.application.dto.CommentDTO;
import com.forum.application.dto.ReplyDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ForumController {

    // @MessageMapping("/posts/{postId}/comments")
    @SendTo("/discussion/posts/{postId}/comments")
    public CommentDTO broadcastComment(@Payload CommentDTO commentDTO,
                                       @DestinationVariable int postId) {
        return commentDTO;
    }

    // @MessageMapping("posts/comments/{commentId}/replies")
    @SendTo("/discussion/posts/comments/{commentId}/replies")
    public ReplyDTO broadcastReply(@Payload ReplyDTO replyDTO,
                                   @DestinationVariable int commentId) {
        return replyDTO;
    }
}
