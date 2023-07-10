package com.forum.application.controller;

import com.forum.application.dto.CommentDTO;
import com.forum.application.dto.ReplyDTO;
import com.forum.application.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@RestController
@RequiredArgsConstructor
@RequestMapping("/forum/api/views")
public class DynamicViewController {

    private final UserService userService;

    @PostMapping("/getCommentBlock")
    public ModelAndView getCommentBlock(@RequestBody CommentDTO commentDto) {
        ModelAndView modelAndView = new ModelAndView("/fragments/comment-body");
        int currentUserId = userService.getCurrentUser().getId();
        modelAndView.addObject("currentUserId", currentUserId);
        modelAndView.addObject("commentDto", commentDto);
        return modelAndView;
    }

    @PostMapping("/getReplyBlock")
    public ModelAndView getReplyBlock(@RequestBody ReplyDTO replyDto) {
        ModelAndView modelAndView = new ModelAndView("/fragments/reply-body");
        int currentUserId = userService.getCurrentUser().getId();
        modelAndView.addObject("currentUserId", currentUserId);
        modelAndView.addObject("replyDto", replyDto);
        return modelAndView;
    }
}
