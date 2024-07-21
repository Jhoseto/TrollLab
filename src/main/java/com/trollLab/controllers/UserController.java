package com.trollLab.controllers;

import com.trollLab.services.UserService;
import com.trollLab.views.UserDetailsViewModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class UserController {

    private final UserService userService;


    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/user-details")
    public String userDetails(@RequestParam("userId") String userId,
                              @RequestParam("videoUrl") String videoUrl,
                              Model model) {

        // Вземи детайлите на потребителя чрез UserService
        UserDetailsViewModel userDetails = userService.getUserProfile(videoUrl, userId);



        // Добави данните в модела
        model.addAttribute("userProfileImageUrl", userDetails.getProfileImageUrl());
        model.addAttribute("userName", userDetails.getName());
        model.addAttribute("userProfileUrl", userDetails.getProfileUrl());
        model.addAttribute("totalComments", userDetails.getTotalComments());
        model.addAttribute("comments", userDetails.getComments());
        model.addAttribute("replies", userDetails.getReplies());

        return "user-details";
    }

}
