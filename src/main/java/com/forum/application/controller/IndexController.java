package com.forum.application.controller;

import com.forum.application.dto.PostDTO;
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
public class IndexController {

    private final ForumService forumService;

    @GetMapping
    public String goToForum(HttpSession session,
                            Model model) {

        String email = (String) session.getAttribute("email");
        if (email == null) return "redirect:/";

        List<PostDTO> posts = forumService.getAllPost();
        model.addAttribute("posts", posts);
        return "forum";
    }
}
