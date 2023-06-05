package com.forum.application.controller;

import com.forum.application.dto.CommentDTO;
import com.forum.application.dto.Message;
import com.forum.application.service.CommentService;
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

    private final CommentService commentService;

    @MessageMapping("/posts/{postId}/comments")
    @SendTo("/discussion/posts/{postId}/comments")
    public String addComment(@Payload Message message,
                             @DestinationVariable int postId) {

        // Return the saved commentDto here
        log.debug("Post Id: {} Comment Body: {}", postId, message.getBody());
        return "HI";
    }
}
