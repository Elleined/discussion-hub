package com.forum.application.controller;

import com.forum.application.dto.PostDTO;
import com.forum.application.model.User;
import com.forum.application.service.ForumService;
import com.forum.application.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

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

        User currentUser = userService.getCurrentUser();
        List<PostDTO> posts = forumService.getAllPost();
        long totalNotifCount = forumService.getAllUnreadNotificationCount(currentUser.getId());

        model.addAttribute("userId", currentUser.getId());
        model.addAttribute("currentUserId", currentUser.getId());
        model.addAttribute("posts", posts);
        model.addAttribute("totalNotifCount", totalNotifCount);
        return "forum";
    }

    @GetMapping("/users/authorPost")
    public String goToPost(HttpSession session,
                           Model model) {

        String email = (String) session.getAttribute("email");
        if (email == null) return "redirect:/";

        int currentUserId = userService.getCurrentUser().getId();
        List<PostDTO> posts = forumService.getAllByAuthorId(currentUserId);
        model.addAttribute("posts", posts);
        model.addAttribute("currentUserId", currentUserId);
        return "author-posts";
    }
}
