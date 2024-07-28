package com.trollLab.controllers;

import com.trollLab.services.FacebookService;
import com.trollLab.views.FacebookCommentViewModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class FacebookController {


    private final FacebookService facebookService;

    @Autowired
    public FacebookController(FacebookService facebookService) {
        this.facebookService = facebookService;
    }


    @GetMapping("/facebookComments")
    public String getFacebookComments(@RequestParam("postUrl") String postUrl, Model model) {
        // Extract post ID from URL
        String postId = extractPostIdFromUrl(postUrl);

        List<FacebookCommentViewModel> comments = facebookService.getCommentsFromPost(postId);
        model.addAttribute("comments", comments);
        return "comments";
    }

    private String extractPostIdFromUrl(String postUrl) {
        // Implement logic to extract post ID from the URL
        return postUrl.substring(postUrl.lastIndexOf("/") + 1);
    }
}
