package com.forum.application.controller;

import com.forum.application.dto.CommentDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.util.HtmlUtils;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ForumController {

    @MessageMapping("/posts/{postId}/comments")
    @SendTo("/discussion/posts/{postId}/comments")
    public CommentDTO addComment(@Payload CommentDTO commentDTO,
                                 @DestinationVariable int postId) {

        commentDTO.setBody(HtmlUtils.htmlEscape(commentDTO.getBody()));
        return commentDTO;
    }
}
