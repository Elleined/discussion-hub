package com.forum.application.controller;

import com.forum.application.dto.CommentResponse;
import com.forum.application.dto.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.util.HtmlUtils;

@Slf4j
@Controller
public class ForumController {

    @MessageMapping("/posts/{postId}/comments")
    @SendTo("/discussion/posts/{postId}/comments")
    public CommentResponse addComment(@Payload Message message,
                                      @DestinationVariable int postId) {

        log.debug("Post Id: {} Comment Body: {}", postId, message.getBody());
        return new CommentResponse(HtmlUtils.htmlEscape(message.getBody()));
    }
}
