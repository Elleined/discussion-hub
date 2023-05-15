package com.forum.application.controller;

import com.forum.application.dto.CommentDTO;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.util.HtmlUtils;

@Slf4j
@Controller
public class ChatController {

    @MessageMapping("/posts/{postId}/comments")
    @SendTo("/forum/posts/{postId}/comments")
    public CommentDTO comment(@DestinationVariable int postId,
                              @Payload CommentDTO commentDTO) {

        log.debug("Post id: {} Comment Body: {}", postId, commentDTO.getBody());
        // Return a full detailed commentDTO
        commentDTO.setBody(HtmlUtils.htmlEscape(commentDTO.getBody()));
        return commentDTO; // Set the content of the DTO to be send in the clients
    }
}
