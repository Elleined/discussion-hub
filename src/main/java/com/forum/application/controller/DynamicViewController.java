package com.forum.application.controller;

import com.forum.application.dto.CommentDTO;
import com.forum.application.dto.NotificationResponse;
import com.forum.application.dto.ReplyDTO;
import com.forum.application.model.User;
import com.forum.application.service.ForumService;
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
    private final ForumService forumService;

    @PostMapping("/getCommentBlock")
    public ModelAndView getCommentBlock(@RequestBody CommentDTO commentDto) {
        User currentUser = userService.getCurrentUser();
        boolean isUserAlreadyLikedComment = forumService.isUserAlreadyLikedComment(currentUser, commentDto.getId());
        return new ModelAndView("/fragments/comment-body")
                .addObject("isUserAlreadyLikedComment", isUserAlreadyLikedComment)
                .addObject("currentUserId", currentUser.getId())
                .addObject("commentDto", commentDto);
    }

    @PostMapping("/getReplyBlock")
    public ModelAndView getReplyBlock(@RequestBody ReplyDTO replyDto) {
        User currentUser = userService.getCurrentUser();
        boolean isUserAlreadyLikeReply = forumService.isUserAlreadyLikeReply(currentUser, replyDto.getId());
        return new ModelAndView("/fragments/reply-body")
                .addObject("isUserAlreadyLikeReply", isUserAlreadyLikeReply)
                .addObject("currentUserId", currentUser.getId())
                .addObject("replyDto", replyDto);
    }

    @PostMapping("/getNotificationBlock")
    public ModelAndView getNotificationBlock(@RequestBody NotificationResponse notification) {
        return new ModelAndView("/fragments/notification-body")
                .addObject("notification", notification);
    }
}
