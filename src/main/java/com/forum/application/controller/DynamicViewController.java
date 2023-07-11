package com.forum.application.controller;

import com.forum.application.dto.CommentDTO;
import com.forum.application.dto.NotificationResponse;
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
        int currentUserId = userService.getCurrentUser().getId();
        return new ModelAndView("/fragments/comment-body")
                .addObject("currentUserId", currentUserId)
                .addObject("commentDto", commentDto);
    }

    @PostMapping("/getReplyBlock")
    public ModelAndView getReplyBlock(@RequestBody ReplyDTO replyDto) {
        int currentUserId = userService.getCurrentUser().getId();
        return new ModelAndView("/fragments/reply-body")
                .addObject("currentUserId", currentUserId)
                .addObject("replyDto", replyDto);
    }

    @PostMapping("/getNotificationBlock")
    public ModelAndView getNotificationBlock(@RequestBody NotificationResponse notification) {
        return new ModelAndView("/fragments/notification-body")
                .addObject("notification", notification);
    }

    @PostMapping("/getMentionBlock")
    public ModelAndView getMentionBlock(@RequestBody NotificationResponse mentionResponse) {
        return new ModelAndView("/fragments/mention-notification")
                .addObject("mention", mentionResponse);
    }
}
