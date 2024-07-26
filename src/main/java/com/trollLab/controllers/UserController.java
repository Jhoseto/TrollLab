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
    private final CommentController commentController;


    @Autowired
    public UserController(UserService userService,
                          CommentController commentController) {
        this.userService = userService;
        this.commentController = commentController;
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


    @GetMapping("/searchByUser")
    public String searchByUser(@RequestParam("userId") String userName,
                               @RequestParam("videoUrl") String videoUrl,
                               @RequestParam(value = "pageToken", required = false) String pageToken,
                               @RequestParam(value = "page", required = false, defaultValue = "1") int page,
                               @RequestParam(value = "sort", required = false, defaultValue = "newest") String sort,
                              Model model) {

        UserDetailsViewModel userDetails = userService.getUserProfile(videoUrl, userName);

        if (userDetails.getComments().isEmpty()){
         commentController.getComments(videoUrl, pageToken, page, sort, model);
         String error = "No user found with this Username in the comments";
            model.addAttribute("error", error);

            return "index";
        } else {
            model.addAttribute("userProfileImageUrl", userDetails.getProfileImageUrl());
            model.addAttribute("userName", userDetails.getName());
            model.addAttribute("userProfileUrl", userDetails.getProfileUrl());
            model.addAttribute("totalComments", userDetails.getTotalComments());
            model.addAttribute("comments", userDetails.getComments());
            model.addAttribute("replies", userDetails.getReplies());

            return "user-details";
        }
    }

}
