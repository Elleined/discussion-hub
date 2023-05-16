package com.forum.application.controller;

import com.forum.application.dto.CommentDTO;
import com.forum.application.dto.PostDTO;
import com.forum.application.service.ForumService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Controller
public class ForumController {

    private final ForumService forumService;

    @MessageMapping("/posts/{postId}/comments")
    @SendTo("/discussion/posts/{postId}/comments")
    public CommentDTO comment(@DestinationVariable int postId,
                              @Payload CommentDTO commentDTO) {

        log.debug("Post id: {} Comment Body: {}", postId, commentDTO.getBody());
        commentDTO.setBody(HtmlUtils.htmlEscape(commentDTO.getBody()));
        return commentDTO; // Return a full detailed commentDTO
    }
}
