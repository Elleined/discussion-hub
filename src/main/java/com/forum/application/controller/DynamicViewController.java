package com.forum.application.controller;

import com.forum.application.dto.CommentDTO;
import com.forum.application.service.ForumService;
import com.forum.application.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@RestController
@RequiredArgsConstructor
@RequestMapping("/forum/api/views")
public class DynamicViewController {

    private final UserService userService;
    private final ForumService forumService;

    @GetMapping("/getCommentBlock")
    public ModelAndView getCommentBlock(@PathVariable("commentId") int commentId) {
        ModelAndView modelAndView = new ModelAndView("/fragments/comment-body");

        int currentUserId = userService.getCurrentUser().getId();
        CommentDTO commentDto = forumService.getCommentById(commentId);
        modelAndView.addObject("currentUserId", currentUserId);
        modelAndView.addObject("commentDto", commentDto);
        return modelAndView;
    }
}
