package com.forum.application.controller;

import com.forum.application.dto.UserDTO;
import com.forum.application.service.ForumService;
import com.forum.application.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/forum/users/blockedUsers")
@RequiredArgsConstructor
public class BlockedController {

    private final ForumService forumService;
    private final UserService userService;
    @GetMapping
    public String goToBlockUsersPage(HttpSession session,
                                     Model model) {

        String email = (String) session.getAttribute("email");
        if (email == null) return "redirect:/";

        int currentUserId = userService.getCurrentUser().getId();
        Set<UserDTO> blockedUsers = forumService.getAllBlockedUsers(currentUserId);
        List<UserDTO> users = forumService.getAllUser(currentUserId);

        model.addAttribute("users", users);
        model.addAttribute("blockedUsers", blockedUsers);
        model.addAttribute("currentUserId", currentUserId);
        return "blocked-users";
    }
}
