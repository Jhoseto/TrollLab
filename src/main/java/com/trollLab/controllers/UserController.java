package com.trollLab.controllers;

import com.trollLab.services.UserService;
import com.trollLab.views.UserDetailsViewModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class UserController {


    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/user-details")
    public String userDetails(@RequestParam("userId") String userId, Model model) {
        UserDetailsViewModel userDetails = userService.getUserDetails(userId);
        model.addAttribute("user", userDetails);
        return "user-details"; // Името на HTML шаблона с подробности за потребителя
    }
}

