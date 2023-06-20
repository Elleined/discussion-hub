package com.forum.application.controller;

import com.forum.application.dto.CommentDTO;
import com.forum.application.dto.ReplyDTO;
import com.forum.application.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/forum/api/users")
public class UserController {
    private final UserService userService;

    @GetMapping("/{userId}/unreadComments")
    public List<CommentDTO> getAllUnreadComments(@PathVariable("userId") int userId) {
        return userService.getAllUnreadCommentsOf(userId);
    }

    @GetMapping("/{userId}/unreadReplies")
    public List<ReplyDTO> getAllUnreadReplies(@PathVariable("userId") int userId) {
        return userService.getAllUnreadReplyOf(userId);
    }
}
