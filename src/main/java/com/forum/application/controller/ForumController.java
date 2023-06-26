package com.forum.application.controller;

import com.forum.application.dto.PostDTO;
import com.forum.application.dto.notification.NotificationResponse;
import com.forum.application.service.ForumService;
import com.forum.application.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("/forum")
public class ForumController {

    private final ForumService forumService;
    private final UserService userService;

    @GetMapping
    public String goToForum(HttpSession session,
                            Model model) {

        String email = (String) session.getAttribute("email");
        if (email == null) return "redirect:/";

        int userId = userService.getIdByEmail(email);
        List<PostDTO> posts = forumService.getAllPost();
        Set<NotificationResponse> notifications = forumService.getAllNotification(userId);
        long totalNotifCount = forumService.getAllUnreadNotificationCount(userId);

        model.addAttribute("userId", userId);
        model.addAttribute("posts", posts);
        model.addAttribute("totalNotifCount", totalNotifCount);
        model.addAttribute("notifications", notifications);
        return "forum";
    }

    @GetMapping("/posts/{authorId}")
    public String goToPost(@PathVariable("authorId") int authorId,
                           HttpSession session,
                           Model model) {

        String email = (String) session.getAttribute("email");
        if (email == null) return "redirect:/";

        List<PostDTO> posts = forumService.getAllByAuthorId(authorId);
        model.addAttribute("posts", posts);
        model.addAttribute("userId", authorId);
        return "author-posts";
    }
}
