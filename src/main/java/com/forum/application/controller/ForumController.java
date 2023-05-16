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

    @GetMapping("/forum")
    public String goToForum(HttpSession session,
                            Model model) {

        String email = (String) session.getAttribute("email");
        if (email == null) return "redirect:/";

        List<PostDTO> posts = forumService.getAllPost();
        model.addAttribute("posts", posts);
        return "forum";
    }

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
