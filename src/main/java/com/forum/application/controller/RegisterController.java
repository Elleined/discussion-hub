package com.forum.application.controller;

import com.forum.application.model.User;
import com.forum.application.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("/register")
public class RegisterController {

    private final UserService userService;

    @GetMapping
    public String goToRegister(@ModelAttribute User user) {
        return "register";
    }

    @PostMapping
    public String register(@ModelAttribute User user,
                           Model model) {

        if (userService.isEmailExists(user.getEmail())) {
            model.addAttribute("emailExists", true);
            model.addAttribute("emailErrorMessage", "Email Already associated with another user!");
            return "register";
        }

        userService.save(user);
        return "redirect:/";
    }
}
