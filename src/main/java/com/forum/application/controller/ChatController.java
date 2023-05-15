package com.forum.application.controller;

import com.forum.application.dto.CommentDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;

@Slf4j
@Controller
public class ChatController {

    @MessageMapping("/posts/{postId}/comments")
    @SendTo("/posts/{postId}/comments")
    public CommentDTO comment(@DestinationVariable int postId,
                              @RequestBody CommentDTO commentDTO) {

        log.debug("Post id: {} Comment Body: {}", postId, commentDTO.getBody());
        // Return a full detailed commentDTO
        return commentDTO;
    }
}
