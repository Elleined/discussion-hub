package com.forum.application.controller;

import com.forum.application.service.ForumService;
import com.forum.application.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@RestController
@RequiredArgsConstructor
@RequestMapping("/forum/api/views")
public class DynamicViewController {

    private final ForumService forumService;
    private final UserService userService;

    @GetMapping("/getCommentBlock/{commentId}")
    public ModelAndView getCommentBlock(@PathVariable("commentId") int commentId) {
        ModelAndView modelAndView = new ModelAndView("/fragments/comment-body");

        int currentUserId = userService.getCurrentUser().getId();
        modelAndView.addObject("currentUserId", currentUserId);
        modelAndView.addObject("commentDto", forumService.getCommentById(commentId));
        return modelAndView;
    }
}
