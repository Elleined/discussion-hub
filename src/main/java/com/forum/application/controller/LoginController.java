package com.forum.application.controller;

import com.forum.application.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping
public class LoginController {
    private final UserService userService;

    @GetMapping
    public String gotoLogin(HttpSession session, Model model) {

        // For Test only

        session.setAttribute("email", "user1@gmail.com");
        // For test only
        String email = (String) session.getAttribute("email");
        if (email != null) return "redirect:/forum";
        return "login";
    }

    @PostMapping
    public String login(@RequestParam String email,
                           HttpSession session,
                           Model model) {

        if (!userService.isEmailExists(email)) {
            model.addAttribute("emailNotExists", true);
            model.addAttribute("emailErrorMessage", "Email Not Exists");
            return "login";
        }

        session.setAttribute("email", email);
        log.debug("{} registered successfully", email);

        return "redirect:/forum";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {

        String email = (String) session.getAttribute("email");
        log.debug("{} logout Successfully", email);
        session.invalidate();

        return "redirect:/";
    }
}
