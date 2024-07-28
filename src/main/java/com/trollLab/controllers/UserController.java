package com.trollLab.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trollLab.services.UserService;
import com.trollLab.services.serviceImpl.UserServiceImpl;
import com.trollLab.views.UserDetailsViewModel;
import com.trollLab.views.YouTubeCommentViewModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class UserController {

    private final UserService userService;
    private final YoutubeController youtubeController;



    @Autowired
    public UserController(UserService userService,
                          YoutubeController youtubeController) {
        this.userService = userService;
        this.youtubeController = youtubeController;

    }



    @GetMapping("/user-details")
    public String userDetails(@RequestParam("userId") String userId,
                              @RequestParam("videoUrl") String videoUrl,
                              Model model) throws JsonProcessingException {

        UserDetailsViewModel userDetails = userService.getUserProfile(videoUrl, userId);

        List<YouTubeCommentViewModel> allComments = new ArrayList<>(userDetails.getComments());
        allComments.addAll(userDetails.getReplies());

        // Сортиране на коментарите по дата
        allComments.sort(Comparator.comparing(YouTubeCommentViewModel::getPublishedAt));

        List<String> formattedDates = allComments.stream()
                .map(YouTubeCommentViewModel::getPublishedAt)
                .collect(Collectors.toList());

        ObjectMapper objectMapper = new ObjectMapper();
        String formattedDatesJson = objectMapper.writeValueAsString(formattedDates);
        String commentsJson = objectMapper.writeValueAsString(allComments);

        // Логване на данните
        System.out.println("Formatted Dates JSON: " + formattedDatesJson);
        System.out.println("Comments JSON: " + commentsJson);

        model.addAttribute("userProfileImageUrl", userDetails.getProfileImageUrl());
        model.addAttribute("userName", userDetails.getName());
        model.addAttribute("userProfileUrl", userDetails.getProfileUrl());
        model.addAttribute("totalComments", userDetails.getTotalComments());
        model.addAttribute("comments", userDetails.getComments());
        model.addAttribute("replies", userDetails.getReplies());
        model.addAttribute("formattedDates", formattedDatesJson);
        model.addAttribute("commentsJson", commentsJson);

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
         youtubeController.getComments(videoUrl, pageToken, page, sort, model);
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
