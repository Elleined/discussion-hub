package com.forum.application.controller;

import com.forum.application.dto.CommentDTO;
import com.forum.application.dto.NotificationResponse;
import com.forum.application.dto.PostDTO;
import com.forum.application.dto.ReplyDTO;
import com.forum.application.model.User;
import com.forum.application.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@RestController
@RequiredArgsConstructor
@RequestMapping("/forum/api/views")
public class DynamicViewController {

    private final UserService userService;

    @PostMapping("/getPostBlock")
    public ModelAndView getPostBlock(@RequestBody PostDTO postDto) {
        User currentUser = userService.getCurrentUser();
        return new ModelAndView("/fragments/post-body")
                .addObject("currentUserId", currentUser.getId())
                .addObject("post", postDto);
    }

    @PostMapping("/getCommentBlock")
    public ModelAndView getCommentBlock(@RequestBody CommentDTO commentDto) {
        User currentUser = userService.getCurrentUser();
        return new ModelAndView("/fragments/comment-body")
                .addObject("currentUserId", currentUser.getId())
                .addObject("commentDto", commentDto);
    }

    @PostMapping("/getReplyBlock")
    public ModelAndView getReplyBlock(@RequestBody ReplyDTO replyDto) {
        User currentUser = userService.getCurrentUser();
        return new ModelAndView("/fragments/reply-body")
                .addObject("currentUserId", currentUser.getId())
                .addObject("replyDto", replyDto);
    }

    @PostMapping("/getNotificationBlock")
    public ModelAndView getNotificationBlock(@RequestBody NotificationResponse notification) {
        return new ModelAndView("/fragments/notification-body")
                .addObject("notification", notification);
    }

    @PostMapping("/getLikeIcon")
    public ModelAndView getLikeIcon(@RequestParam("isLiked") boolean isLiked) {
        return new ModelAndView("/fragments/like-icon").addObject("isLiked", isLiked);
    }
}
